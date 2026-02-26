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
    public String register(HttpSession session, @ModelAttribute("registerUser") UserDTO userRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "client/admin/register";
        }
        // this.userService.register(userRequest);
        if (this.userRepository.existsByEmail(userRequest.getEmail())) {
            bindingResult.rejectValue("email", "CONFLICT", "Email already exists!");
            return "client/auth/register";
        }
        var otp = this.userService.generateBase();
        session.setAttribute("PENDING_USER", userRequest);
        session.setAttribute("OTP_CODE", otp);
        session.setAttribute("OTP_EMAIL", userRequest.getEmail());
        session.setAttribute("OTP_EXPIRE", LocalDateTime.now().plusMinutes(5));
        this.mailService.startOTP(userRequest.getEmail(), otp);
        return "redirect:/verify";
    }

    @GetMapping("/access-deny")
    public String getAccessDenyPage() {
        return "client/auth/access-deny";
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "client/auth/verify";
    }

    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request, HttpSession session) {
        var otp = request.get("otp");
        var email = request.get("email");

        var sOTP = (String) session.getAttribute("OTP_CODE");
        var sEmail = (String) session.getAttribute("OTP_EMAIL");
        var expire = (LocalDateTime) session.getAttribute("OTP_EXPIRE");

        if (sOTP.equals(otp) && sEmail.equals(email)) {
            UserDTO pendingUser = (UserDTO) session.getAttribute("PENDING_USER");

            if (pendingUser != null) {
                this.userService.register(pendingUser);
                session.removeAttribute("PENDING_USER");
                session.removeAttribute("OTP_CODE");
                session.removeAttribute("OTP_EMAIL");

                return ResponseEntity.ok(Map.of("success", true));
            }
        }
        return ResponseEntity.badRequest().body("INVALID_DATA");
    }
}
