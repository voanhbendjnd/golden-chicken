package vn.edu.fpt.golden_chicken.utils.converts;

import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.request.RoleRequest;
import vn.edu.fpt.golden_chicken.domain.response.RoleRes;

public class RoleConvert {
    public static Role toRole(RoleRequest request) {
        var role = new Role();
        role.setDescription(request.getDescription());
        role.setName(request.getName());
        return role;
    }

    public static RoleRes toRoleRes(Role role) {
        var res = new RoleRes();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        return res;
    }
}
