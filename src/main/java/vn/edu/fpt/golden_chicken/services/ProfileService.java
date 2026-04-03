package vn.edu.fpt.golden_chicken.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return null;
        return userRepository.findByEmailIgnoreCase(auth.getName());
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

    public String updateAvatar(String fileName) {
        User user = getCurrentUser();
        if (user == null || fileName == null || fileName.isEmpty())
            return null;
        String oldAvatar = user.getAvatar();
        user.setAvatar(fileName);
        userRepository.save(user);
        return oldAvatar;
    }

    public record ChangePasswordResult(boolean success, String message) {
    }

    public ChangePasswordResult changePassword(String oldPassword, String newPassword) {

        User user = getCurrentUser();
        if (user == null) {
            return new ChangePasswordResult(false, "Bạn cần đăng nhập để đổi mật khẩu.");
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            return new ChangePasswordResult(false, "Vui lòng nhập mật khẩu cũ.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            return new ChangePasswordResult(false, "Vui lòng nhập mật khẩu mới.");
        }

        // optional: rule mạnh hơn
        if (newPassword.length() < 6) {
            return new ChangePasswordResult(false, "Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return new ChangePasswordResult(false, "Mật khẩu cũ không đúng.");
        }

        // optional: không cho đặt trùng mật khẩu cũ
        // if (passwordEncoder.matches(newPassword, user.getPassword())) {
        // return new ChangePasswordResult(false, "Mật khẩu mới không được trùng mật
        // khẩu cũ.");
        // }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new ChangePasswordResult(true, "Đổi mật khẩu thành công!");
    }

    public boolean checkOldPassword(String oldPassword) {
        User user = getCurrentUser();
        if (user == null)
            return false;
        if (oldPassword == null || oldPassword.isBlank())
            return false;
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
