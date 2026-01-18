package vn.edu.fpt.golden_chicken.utils.converts;

import vn.edu.fpt.golden_chicken.domain.entity.Permission;
import vn.edu.fpt.golden_chicken.domain.request.PermissionDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResPermission;

public class PermissionConvert {
    public static Permission toPermission(PermissionDTO dto) {
        var permission = new Permission();
        permission.setApiPath(dto.getApiPath());
        permission.setName(dto.getName());
        permission.setMethod(dto.getMethod());
        permission.setModule(dto.getModule());
        return permission;
    }

    public static ResPermission toResPermissinon(Permission permission) {
        var res = new ResPermission();
        res.setApiPath(permission.getApiPath());
        res.setId(permission.getId());
        res.setMethod(permission.getMethod());
        res.setModule(permission.getModule());
        res.setName(permission.getName());
        return res;
    }
}
