package vn.edu.fpt.golden_chicken.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.ProfileUpdateDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResUser;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return null;
        return userRepository.findByEmail(auth.getName().toLowerCase());
    }

    public ProfileUpdateDTO getProfileForm() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        return dto;
    }

    public ResUser getProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        ResUser res = new ResUser();
        res.setId(user.getId());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());
        res.setCreatedBy(user.getCreatedBy());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setUpdatedBy(user.getUpdatedBy());
        res.setAvatar(user.getAvatar());
        if (user.getRole() != null) {
            res.setRoleId(user.getRole().getId());
        }
        if (user.getStaff() != null) {
            res.setStaffType(user.getStaff().getStaffType());
        }
        return res;
    }

    public void updateProfile(ProfileUpdateDTO dto) {
        if (dto == null) {
            return;
        }
        User user = getCurrentUser();
        if (user == null) {
            return;
        }
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        userRepository.save(user);
    }

    public void updateAvatar(String fileName) {
        User user = getCurrentUser();
        if (user == null || fileName == null || fileName.isEmpty()) return;
        user.setAvatar(fileName);
        userRepository.save(user);
    }
}
