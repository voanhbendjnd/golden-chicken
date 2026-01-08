package vn.edu.fpt.golden_chicken.utils.converts;

import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserRequest;

public class UserConvert {
    public static User toUser(UserRequest request) {
        var user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        return user;
    }
}
