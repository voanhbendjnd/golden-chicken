package vn.edu.fpt.golden_chicken.utils.converts;

import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResUser;

public class UserConvert {
    public static User toUser(UserDTO request) {
        var user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setStatus(request.getStatus());
        user.setPhone(request.getPhone());
        return user;
    }

    public static ResUser toUserRes(User user) {
        var res = new ResUser();
        res.setStatus(user.getStatus());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setId(user.getId());
        res.setPhone(user.getPhone());
        res.setRoleId(user.getRole().getId());
        if (user.getStaff() != null) {
            res.setStaffType(user.getStaff().getStaffType());
        }
        return res;
    }
}
