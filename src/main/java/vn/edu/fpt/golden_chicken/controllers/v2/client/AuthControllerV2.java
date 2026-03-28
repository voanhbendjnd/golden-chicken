package vn.edu.fpt.golden_chicken.controllers.v2.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.response.VerifyAccountMessage;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.RateLimitService;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
/*
 ** Djnd config change password v2 save at redis
 */
public class AuthControllerV2 {
    UserService userService;
    RedisUserService redisUserService;
    KafkaTemplate<String, VerifyAccountMessage> kafkaVerifyMessage;
    UserRepository userRepository;
    RateLimitService rateLimitService;

    @GetMapping("/forgot-password-v2")
    public String forgotPasswordPage(Model model) {
        return "client/auth/forgot.password.v2";
    }

    @PostMapping("/forgot-password-v2")
    public String forgotPassword(@RequestParam("email") String email, RedirectAttributes re) {
        // if (this.userRepository.existsByEmailIgnoreCase(email)) {
        return "redirect:/verify-v2?email=" + email;

        // }
        // re.addFlashAttribute("error", "Email not exists!");
        // return "redirect:/forgot-password-v2";
    }

    @GetMapping("verify-v2")
    public String verifyPage(@RequestParam("email") String email, Model model, HttpSession session) {
        model.addAttribute("email", email);

        if (this.rateLimitService.tryConsume(email)) {
            var oneTimePassword = this.userService.generateBase();
            this.redisUserService.saveKeyOTPForgotPassword(email, oneTimePassword);
            var verifyMsg = new VerifyAccountMessage();
            verifyMsg.setDescription("Send OTP for update password");
            verifyMsg.setCreatedAt(LocalDateTime.now());
            verifyMsg.setEmail(email);
            long expireAt = Instant.now().getEpochSecond() + 3 * 60;

            session.setAttribute("FP:OTP", oneTimePassword);
            session.setAttribute("FP:EXPIREAT", expireAt);
            this.kafkaVerifyMessage.send("forgot-password-account-topic", verifyMsg);
        } else {
            model.addAttribute("rateLimitMessage", "Vui lòng đợi 1 phút trước khi yêu cầu gửi lại OTP.");
        }

        // Đồng bộ thời gian còn lại thực tế trong Redis
        long actualTtl = this.redisUserService.getKeyOTPForgotPasswordTtl(email);
        model.addAttribute("otpTtlSeconds", actualTtl > 0 ? actualTtl : 0);

        return "client/auth/verify.v2";
    }

    @PostMapping("/verify-v2")
    @ResponseBody
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request, HttpSession session) {
        String otp = request.get("otp");
        String email = request.get("email");
        if (this.redisUserService.checkOTPForgotPassword(email, otp)) {
            long now = Instant.now().getEpochSecond();
            var ex = (Long) session.getAttribute("FP:EXPIREAT");
            var ottp = (String) session.getAttribute("FP:OTP");
            if (now <= ex && ottp.equals(otp)) {
                this.userService.allowUpdatePassword(email);
                session.setAttribute("EMAIL:TOKEN", email);
                session.setAttribute("OTP:TOKEN", otp);
                return ResponseEntity.ok(Map.of("success", true, "message", "Registration successful!"));
            }

        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid OTP code."));
    }

    @GetMapping("/update-password-v2")
    public String updatePage() {
        return "client/auth/reset.password.v2";
    }

    @PostMapping("/update-password-v2")
    public String updatePassword(@RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword, HttpSession session, RedirectAttributes re) {
        if (!password.equals(confirmPassword)) {
            re.addFlashAttribute("error", "Password does not match!");
            return "redirect:/update-password-v2";
        }
        long now = Instant.now().getEpochSecond();
        var ex = (Long) session.getAttribute("FP:EXPIREAT");
        if (now <= ex) {
            var otpToken = (String) session.getAttribute("OTP:TOKEN");
            var emailToken = (String) session.getAttribute("EMAIL:TOKEN");
            if (!ObjectUtils.isEmpty(this.redisUserService.getKeyOTPForgotPassword(emailToken))) {
                if (this.redisUserService.getKeyOTPForgotPassword(emailToken).equals(otpToken)
                        && this.userService.getAllowChangePassword(emailToken)) {
                    this.userService.changePassword(emailToken, confirmPassword);
                    return "redirect:/login";
                }
            }

        }
        return "redirect:/update-password-v2";
    }

}
