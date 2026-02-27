package vn.edu.fpt.golden_chicken.controllers.client;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.MailService;
import vn.edu.fpt.golden_chicken.services.UserService;

@Controller
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final MailService mailService;

    public AuthController(MailService mailService, UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.mailService = mailService;
        this.userRepository = userRepository;
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
        var otp = this.userService.generateBase();
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
        this.mailService.startOTP(email, OTP);
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
}
