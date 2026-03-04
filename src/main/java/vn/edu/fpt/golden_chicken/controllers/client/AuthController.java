package vn.edu.fpt.golden_chicken.controllers.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.domain.response.VerifyAccountMessage;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.MailService;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.services.redis.RedisOTPService;

@Controller
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final RedisOTPService redisOTPService;

    @Autowired
    private KafkaTemplate<String, VerifyAccountMessage> verifyAccountKafka;

    public AuthController(MailService mailService, RedisOTPService redisOTPService, UserService userService,
            UserRepository userRepository) {
        this.userService = userService;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.redisOTPService = redisOTPService;
    }

    @GetMapping("/login")
    public String login() {
        return "client/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerUser", new UserDTO());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String register(HttpServletRequest request, @ModelAttribute("registerUser") UserDTO userRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "client/auth/register";
        }
        // this.userService.register(userRequest);
        if (this.userRepository.existsByEmail(userRequest.getEmail())) {
            bindingResult.rejectValue("email", "CONFLICT", "Email already exists!");
            return "client/auth/register";
        }
        var session = request.getSession(true);
        // var otp = this.userService.generateBase();
        session.setAttribute("PENDING_USER", userRequest);
        // session.setAttribute("OTP_CODE", otp);
        session.setAttribute("OTP_EMAIL", userRequest.getEmail());
        // session.setAttribute("OTP_EXPIRE", LocalDateTime.now().plusMinutes(5));
        // this.mailService.startOTP(userRequest.getEmail(), otp);
        return "redirect:/verify";
    }

    @GetMapping("/access-deny")
    public String getAccessDenyPage() {
        return "client/auth/access-deny";
    }

    @GetMapping("/verify")
    public String verifyPage(HttpServletRequest request) {
        var session = request.getSession(true);
        var OTP = this.userService.generateBase();
        session.setAttribute("OTP_CODE", OTP);
        session.setAttribute("OTP_EXPIRE", LocalDateTime.now().plusMinutes(5));
        var email = (String) request.getSession().getAttribute("OTP_EMAIL");
        if (email == null || email.isEmpty()) {
            return "redirect:/login";
        }
        this.redisOTPService.saveOTP(email, OTP);
        // this.mailService.startOTP(email, OTP);
        var msg = new VerifyAccountMessage();
        msg.setDescription("Verify Account");
        msg.setCreatedAt(LocalDateTime.now());
        msg.setEmail(email);

        // msg.setOtp(OTP);
        this.verifyAccountKafka.send("customer-account-topic", msg);
        return "client/auth/verify";
    }

    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request, HttpSession session) {
        String otp = request.get("otp");
        String email = request.get("email");

        String sOTP = (String) session.getAttribute("OTP_CODE");
        String sEmail = (String) session.getAttribute("OTP_EMAIL");
        LocalDateTime expire = (LocalDateTime) session.getAttribute("OTP_EXPIRE");
        UserDTO pendingUser = (UserDTO) session.getAttribute("PENDING_USER");

        if (sOTP == null || sEmail == null || expire == null || pendingUser == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Session expired or invalid. Please register again."));
        }
        if (LocalDateTime.now().isAfter(expire)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "OTP has expired."));
        }

        if (sOTP.equals(otp) && sEmail.equalsIgnoreCase(email)) {
            try {
                this.userService.register(pendingUser);

                session.removeAttribute("PENDING_USER");
                session.removeAttribute("OTP_CODE");
                session.removeAttribute("OTP_EMAIL");
                session.removeAttribute("OTP_EXPIRE");

                return ResponseEntity.ok(Map.of("success", true, "message", "Registration successful!"));
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("success", false, "message", "Error saving user: " + e.getMessage()));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid OTP code."));
    }

    // Session keys riêng cho forgot-password để tránh xung đột với register verify
    private static final String FP_EMAIL = "FP_EMAIL";
    private static final String FP_OTP = "FP_OTP";
    private static final String FP_EXPIRE_AT = "FP_EXPIRE_AT"; // epoch seconds
    private static final String FP_VERIFIED = "FP_VERIFIED";
    private static final long FP_TTL_SECONDS = 5 * 60; // 5 phút

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "client/auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam("email") String email,
            HttpSession session,
            RedirectAttributes ra) {
        String normalized = normalizeEmail(email);

        if (normalized.isBlank() || !this.userService.existsByEmail(normalized)) {
            ra.addFlashAttribute("error", "Email không tồn tại trong hệ thống.");
            return "redirect:/forgot-password";
        }

        String otp = generateOtp6();
        long expireAt = Instant.now().getEpochSecond() + FP_TTL_SECONDS;

        session.setAttribute(FP_EMAIL, normalized);
        session.setAttribute(FP_OTP, otp);
        session.setAttribute(FP_EXPIRE_AT, expireAt);
        session.setAttribute(FP_VERIFIED, false);

        // dùng MailService có sẵn (giống flow register)
        this.mailService.startOTP(normalized, otp);

        ra.addFlashAttribute("message", "Đã gửi OTP về email. Vui lòng kiểm tra hộp thư.");
        return "redirect:/verify-otp";
    }

    @PostMapping("/forgot-password/resend")
    public String resendForgotOtp(HttpSession session, RedirectAttributes ra) {
        String email = (String) session.getAttribute(FP_EMAIL);
        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("error", "Session đã hết. Vui lòng nhập email lại.");
            return "redirect:/forgot-password";
        }

        String otp = generateOtp6();
        long expireAt = Instant.now().getEpochSecond() + FP_TTL_SECONDS;

        session.setAttribute(FP_OTP, otp);
        session.setAttribute(FP_EXPIRE_AT, expireAt);
        session.setAttribute(FP_VERIFIED, false);

        this.mailService.startOTP(email, otp);

        ra.addFlashAttribute("message", "Đã gửi lại OTP.");
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model, RedirectAttributes ra) {
        String email = (String) session.getAttribute(FP_EMAIL);
        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("error", "Vui lòng nhập email để nhận OTP.");
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", email);
        return "client/auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String handleVerifyOtp(
            @RequestParam("otp") String otp,
            HttpSession session,
            RedirectAttributes ra) {
        String email = (String) session.getAttribute(FP_EMAIL);
        String savedOtp = (String) session.getAttribute(FP_OTP);
        Long expireAt = (Long) session.getAttribute(FP_EXPIRE_AT);

        if (email == null || savedOtp == null || expireAt == null) {
            ra.addFlashAttribute("error", "Session đã hết. Vui lòng thao tác lại.");
            return "redirect:/forgot-password";
        }

        otp = otp == null ? "" : otp.trim();
        if (!otp.matches("\\d{6}")) {
            ra.addFlashAttribute("error", "OTP phải đúng 6 chữ số.");
            return "redirect:/verify-otp";
        }

        long now = Instant.now().getEpochSecond();
        if (now > expireAt) {
            ra.addFlashAttribute("error", "OTP đã hết hạn. Vui lòng gửi lại OTP.");
            return "redirect:/verify-otp";
        }

        if (!savedOtp.equals(otp)) {
            ra.addFlashAttribute("error", "OTP không đúng.");
            return "redirect:/verify-otp";
        }

        session.setAttribute(FP_VERIFIED, true);
        ra.addFlashAttribute("message", "Xác thực OTP thành công. Vui lòng đặt mật khẩu mới.");
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session, Model model, RedirectAttributes ra) {
        String email = (String) session.getAttribute(FP_EMAIL);
        Boolean verified = (Boolean) session.getAttribute(FP_VERIFIED);

        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("error", "Vui lòng nhập email để nhận OTP.");
            return "redirect:/forgot-password";
        }
        if (verified == null || !verified) {
            ra.addFlashAttribute("error", "Bạn cần xác thực OTP trước.");
            return "redirect:/verify-otp";
        }

        model.addAttribute("email", email);
        return "client/auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes ra) {
        String email = (String) session.getAttribute(FP_EMAIL);
        Boolean verified = (Boolean) session.getAttribute(FP_VERIFIED);

        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("error", "Session đã hết. Vui lòng thao tác lại.");
            return "redirect:/forgot-password";
        }
        if (verified == null || !verified) {
            ra.addFlashAttribute("error", "Bạn cần xác thực OTP trước.");
            return "redirect:/verify-otp";
        }

        password = password == null ? "" : password.trim();
        confirmPassword = confirmPassword == null ? "" : confirmPassword.trim();

        if (password.length() < 6) {
            ra.addFlashAttribute("error", "Mật khẩu phải ít nhất 6 ký tự.");
            return "redirect:/reset-password";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Password và Confirm Password không khớp.");
            return "redirect:/reset-password";
        }

        // Update password (encode inside UserService)
        this.userService.updatePasswordByEmail(email, password);

        // clear flow session
        clearForgotSession(session);

        ra.addFlashAttribute("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
        return "redirect:/login";
    }

    // =========================
    // Helpers
    // =========================

    private String normalizeEmail(String email) {
        if (email == null)
            return "";
        return email.trim().toLowerCase();
    }

    private String generateOtp6() {
        int n = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", n);
    }

    private void clearForgotSession(HttpSession session) {
        session.removeAttribute(FP_EMAIL);
        session.removeAttribute(FP_OTP);
        session.removeAttribute(FP_EXPIRE_AT);
        session.removeAttribute(FP_VERIFIED);
    }
}
