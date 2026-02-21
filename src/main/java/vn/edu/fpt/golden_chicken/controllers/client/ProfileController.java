package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.domain.request.ProfileUpdateDTO;
import vn.edu.fpt.golden_chicken.services.AddressServices;
import vn.edu.fpt.golden_chicken.services.ProfileService;
import vn.edu.fpt.golden_chicken.services.UploadService;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final AddressServices addressServices;
    private final UploadService uploadService;

    public ProfileController(ProfileService profileService, AddressServices addressServices,
        UploadService uploadService) {
        this.profileService = profileService;
        this.addressServices = addressServices;
        this.uploadService = uploadService;
    }

    @GetMapping("/profile")
    public String showProfile(
            @RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit,
            Model model) {

        model.addAttribute("isEdit", edit);
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("profileForm", profileService.getProfileForm());
        model.addAttribute("defaultAddress", addressServices.getDefaultAddress());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfileForm(
            @Valid @ModelAttribute("profileForm") ProfileUpdateDTO profileForm,
            BindingResult bindingResult,
            Model model) {

        model.addAttribute("isEdit", true);
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profileService.getProfile());
            return "profile";
        }

        profileService.updateProfile(profileForm);
        return "redirect:/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileName = uploadService.handleSaveUploadFile(avatarFile, "img/avatar");
            profileService.updateAvatar(fileName);
        }
        return "redirect:/profile";
    }
}
