package vn.edu.fpt.golden_chicken.controllers.admin;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;

import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserRequest;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Controller
@RequestMapping("/admin/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAll());
        return "admin/user/list";
    }

    @GetMapping("/create")
    public String createUserPage(Model model) {
        model.addAttribute("newUser", new UserRequest());
        return "admin/user/create"; // dẫn chính xác tới folder + file create.jsp
    }

    @PostMapping("/create")
    public String create(Model model, @ModelAttribute("newUser") @Valid UserRequest user, BindingResult bindingResult) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
        }
        if (bindingResult.hasErrors()) {
            return "admin/user/create";
        }

        this.userService.create(user);
        return "redirect:/admin/user"; // redirect tới mapping
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String updateUserPage(Model model, @PathVariable("id") Long id) {
        User userUpdate = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id + ""));
        model.addAttribute("updateUser", userUpdate);

        return "admin/user/update";
    }

}
