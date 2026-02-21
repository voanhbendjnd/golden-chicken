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
import vn.edu.fpt.golden_chicken.services.FileService;
import vn.edu.fpt.golden_chicken.services.ProfileService;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final AddressServices addressServices;
    private final FileService fileService;

    public ProfileController(ProfileService profileService, AddressServices addressServices,
            FileService fileService) {
        this.fileService = fileService;
        this.profileService = profileService;
        this.addressServices = addressServices;
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
            String fileName = this.fileService.handleSaveUploadFile(avatarFile, "img/avatar");
            profileService.updateAvatar(fileName);
        }
        return "redirect:/profile";
    }
}
