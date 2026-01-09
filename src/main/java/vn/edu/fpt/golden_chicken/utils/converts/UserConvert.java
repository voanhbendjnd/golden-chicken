package vn.edu.fpt.golden_chicken.utils.converts;

import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserRequest;
import vn.edu.fpt.golden_chicken.domain.response.UserRes;

public class UserConvert {
    public static User toUser(UserRequest request) {
        var user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        return user;
    }

    public static UserRes toUserRes(User user) {
        var res = new UserRes();
        res.setAddress(user.getAddress());
        res.setCurrentPoints(user.getCurrentPoints());
        res.setStatus(user.getStatus());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setId(user.getId());
        res.setPhone(user.getPhone());
        return res;
    }
}
