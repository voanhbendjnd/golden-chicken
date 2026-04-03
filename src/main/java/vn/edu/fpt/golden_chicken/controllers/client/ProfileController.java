package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
            String newFileName = this.fileService.handleSaveUploadFile(avatarFile, "img/avatar");
            String oldFileName = profileService.updateAvatar(newFileName);

            if (oldFileName != null
                    && !oldFileName.isBlank()
                    && !oldFileName.equals("testimonial-1.jpg")
                    && !oldFileName.equals(newFileName)) {
                this.fileService.deleteAvatarFile(oldFileName, "img/avatar");
            }
        }
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (!profileService.checkOldPassword(oldPassword)) {
            model.addAttribute("errorOld", "Mật khẩu cũ không đúng.");
            model.addAttribute("focusField", "old");
            return "change-password";
        }

        if (newPassword == null || newPassword.isBlank()) {
            model.addAttribute("errorNew", "Vui lòng nhập mật khẩu mới.");
            model.addAttribute("focusField", "new");
            return "change-password";
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            model.addAttribute("errorConfirm", "Vui lòng nhập xác nhận mật khẩu.");
            model.addAttribute("focusField", "confirm");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorConfirm", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("focusField", "confirm");
            return "change-password";
        }

        var result = profileService.changePassword(oldPassword, newPassword);

        if (!result.success()) {
            String msg = result.message() == null ? "Đổi mật khẩu thất bại." : result.message();
            model.addAttribute("error", msg);

            String lower = msg.toLowerCase();
            if (lower.contains("cũ")) {
                model.addAttribute("focusField", "old");
            } else {
                model.addAttribute("focusField", "new");
            }

            return "change-password";
        }

        model.addAttribute("success", result.message());
        return "change-password";
    }

    @PostMapping("/change-password/check-old")
    @ResponseBody
    public Map<String, Object> checkOld(@RequestParam String oldPassword) {
        boolean valid = profileService.checkOldPassword(oldPassword);
        return Map.of("valid", valid);
    }
}
