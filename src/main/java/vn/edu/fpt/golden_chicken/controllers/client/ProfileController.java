package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.domain.request.ProfileUpdateDTO;
import vn.edu.fpt.golden_chicken.services.ProfileService;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String getProfilePage(
            @RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
            Model model) {

        model.addAttribute("isEdit", edit);
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("profileForm", profileService.getProfileForm());
        return "testProfile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileUpdateDTO profileForm,
            BindingResult bindingResult,
            Model model) {

        model.addAttribute("isEdit", true);
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profileService.getProfile());
            return "testProfile";
        }

        profileService.updateProfile(profileForm);
        return "redirect:/profile";
    }
}
