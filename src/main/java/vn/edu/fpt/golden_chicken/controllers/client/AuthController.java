package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import vn.edu.fpt.golden_chicken.domain.request.UserRequest;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.exceptions.EmailAlreadyExistsException;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "client/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerUser", new UserRequest());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerUser") UserRequest userRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "client/admin/register";
        }
        try {
            this.userService.register(userRequest);

        } catch (EmailAlreadyExistsException ex) {
            bindingResult.rejectValue("email", "CONFLICT", ex.getMessage());
            return "client/auth/register";
        }
        return "redirect:/login";
    }

    @GetMapping("/access-deny")
    public String getAccessDenyPage() {
        return "client/auth/access-deny";
    }
}
