package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.request.RoleDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResRole;
import vn.edu.fpt.golden_chicken.repositories.PermissionRepository;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.utils.converts.RoleConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    UserRepository userRepository;
    PermissionRepository permissionRepository;

    public void create(RoleDTO request) {
        if (this.roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Vai trò là " + request.getName() + " đã tồn tại!");
        }
        var permissions = this.permissionRepository.findByIdIn(request.getPermissionIds());
        var role = new Role();
        role.setDescription(request.getDescription());
        role.setName(request.getName());
        role.setPermissions(permissions);
        this.roleRepository.save(role);

    }

    public ResRole update(RoleDTO request) {
        var id = request.getId();
        Role role = this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role ID", id));

        if (this.roleRepository.existsByNameAndIdNot(request.getName(), request.getId())) {
            throw new RuntimeException("Vai trò với tên là " + request.getName() + " đã tồn tại!");
        }
        var permssions = this.permissionRepository.findByIdIn(request.getPermissionIds());
        role.setPermissions(permssions);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return RoleConvert.toRoleRes(this.roleRepository.save(role));
    }

    public ResRole findById(long id) {
        var role = this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role ID", id));
        return RoleConvert.toRoleRes(role);
    }

    public boolean deleteById(long id) {
        var role = this.roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role ID", id));
        if (this.userRepository.existsByRoleId(id)) {
            return false;
        } else {
            this.roleRepository.delete(role);
            return true;
        }
    }

    public ResultPaginationDTO fetchAllWithPagination(Specification<Role> spec, Pageable pageable) {
        var page = this.roleRepository.findAll(spec, pageable);
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(RoleConvert::toRoleRes).collect(Collectors.toList()));
        return res;
    }

    public List<ResRole> fetchAll() {
        return this.roleRepository.findAll().stream().map(RoleConvert::toRoleRes).collect(Collectors.toList());
    }

    @SuppressWarnings("resource")
    public void importRoles(MultipartFile file) throws IOException, DataFormatException {
        var is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        var sheet = workbook.getSheetAt(0);
        var roles = new ArrayList<Role>();
        var set = this.roleRepository.findAll().stream().map(Role::getName).collect(Collectors.toSet());
        if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
            throw new DataFormatException("File excel không tồn tại!");
        }
        for (var row : sheet) {
            var rowNum = row.getRowNum();
            if (row.getRowNum() == 0) {
                continue;
            }
            if (row.getCell(0) == null || row.getCell(1) == null) {
                throw new DataFormatException("Dữ liệu bị lỗi tại hàng " + (rowNum + 1) + ": quên truyền dữ liệu");
            }
            var role = new Role();
            var name = row.getCell(0).getStringCellValue();
            if (set.contains(name)) {
                throw new DataInvalidException("Role Name With (" + name + ") Already Exists!");
            }
            role.setName(name);
            role.setDescription(row.getCell(1).getStringCellValue());
            roles.add(role);
        }
        this.roleRepository.saveAll(roles);

    }
}
