package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.fpt.golden_chicken.domain.request.UserRequest;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login() {
        return "client/auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerUser", new UserRequest());
        return "client/auth/register";
    }
}
