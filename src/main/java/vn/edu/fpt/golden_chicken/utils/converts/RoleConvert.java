package vn.edu.fpt.golden_chicken.utils.converts;

import java.util.stream.Collectors;

import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.request.RoleDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResRole;

public class RoleConvert {
    public static Role toRole(RoleDTO request) {
        var role = new Role();
        role.setDescription(request.getDescription());
        role.setName(request.getName());
        return role;
    }

    public static ResRole toRoleRes(Role role) {
        var res = new ResRole();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setPermissions(role.getPermissions().stream().map(x -> new ResRole.Permission(x.getId(), x.getName()))
                .collect(Collectors.toList()));
        return res;
    }
}
