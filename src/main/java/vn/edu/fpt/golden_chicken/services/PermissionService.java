package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Permission;
import vn.edu.fpt.golden_chicken.domain.request.PermissionDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResPermission;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.repositories.PermissionRepository;
import vn.edu.fpt.golden_chicken.utils.converts.PermissionConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@RequiredArgsConstructor
public class PermissionService {
    PermissionRepository permissionRepository;

    public void create(PermissionDTO dto) {
        if (this.permissionRepository.existsByNameAndApiPathAndMethodAndModule(dto.getName(), dto.getApiPath(),
                dto.getMethod(), dto.getModule())) {
            throw new DataInvalidException("This Permission Already Exists!");
        }

        this.permissionRepository.save(PermissionConvert.toPermission(dto));
    }

    public void update(PermissionDTO dto) {
        if (this.permissionRepository.existsByNameAndApiPathAndMethodAndModuleAndIdNot(dto.getName(), dto.getApiPath(),
                dto.getMethod(), dto.getModule(), dto.getId())) {
            throw new DataInvalidException("Permissoin Permisson Already Exists!");
        }
        var permisson = this.permissionRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission ID", dto.getId()));
        permisson.setApiPath(dto.getApiPath());
        permisson.setMethod(dto.getMethod());
        permisson.setModule(dto.getModule());
        permisson.setName(dto.getName());
        this.permissionRepository.save(permisson);
    }

    public ResPermission findById(long id) {
        var permission = this.permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission ID", id));
        return PermissionConvert.toResPermissinon(permission);
    }

    public void deleteById(long id) {
        var permission = this.permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission ID", id));
        this.permissionRepository.delete(permission);
    }

    public ResultPaginationDTO fecthAllWithPaginationDTO(Specification<Permission> spec, Pageable pageable) {
        var page = this.permissionRepository.findAll(spec, pageable);
        var res = new ResultPaginationDTO();
        var meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(page.getContent().stream().map(PermissionConvert::toResPermissinon).collect(Collectors.toList()));
        return res;
    }

    public void importPermissions(MultipartFile file) throws IOException {
        var is = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        var sheet = workbook.getSheetAt(0);
        var set = this.permissionRepository.findAll().stream()
                .map(x -> x.getName() + "-" + x.getApiPath() + "-" + x.getMethod() + "-" + x.getModule())
                .collect(Collectors.toSet());
        var permissions = new ArrayList<Permission>();
        for (var row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            var permission = new Permission();
            var name = row.getCell(0).getStringCellValue();
            var apiPath = row.getCell(1).getStringCellValue();
            var method = row.getCell(2).getStringCellValue();
            var module = row.getCell(3).getStringCellValue();
            var key = name + "-" + apiPath + "-" + method + "-" + module;
            if (set.contains(key)) {
                throw new DataInvalidException("This Permission (" + name + ") Already Exists!");
            }
            permission.setName(name);
            permission.setApiPath(apiPath);
            permission.setModule(module);
            permission.setMethod(method);
            permissions.add(permission);
        }
        if (!permissions.isEmpty()) {
            this.permissionRepository.saveAll(permissions);

        }
    }

    public List<ResPermission> fetchAll() {
        return this.permissionRepository.findAll().stream().map(PermissionConvert::toResPermissinon)
                .collect(Collectors.toList());
    }

}
