package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.edu.fpt.golden_chicken.domain.response.ResUser;
import vn.edu.fpt.golden_chicken.services.ProfileService;

@ControllerAdvice
public class GlobalModelAttributes {
    private final ProfileService profileService;

    public GlobalModelAttributes(ProfileService profileService) {
        this.profileService = profileService;
    }

    @ModelAttribute("profile")
    public ResUser profile() {
        return profileService.getProfile();
    }
}
