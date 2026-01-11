package vn.edu.fpt.golden_chicken.services;

import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.request.RoleRequest;
import vn.edu.fpt.golden_chicken.domain.response.ResultPaginationDTO;
import vn.edu.fpt.golden_chicken.domain.response.RoleRes;
import vn.edu.fpt.golden_chicken.repositories.RoleRepository;
import vn.edu.fpt.golden_chicken.utils.converts.RoleConvert;
import vn.edu.fpt.golden_chicken.utils.exceptions.ResourceNotFoundException;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;

    public void create(RoleRequest request) {
        if (this.roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role Name " + request.getName() + " already exists!");
        }
        this.roleRepository.save(RoleConvert.toRole(request));

    }

    public RoleRes update(RoleRequest request) {
        var id = request.getId();
        Role role = this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role ID", id));

        if (this.roleRepository.existsByNameAndIdNot(request.getName(), request.getId())) {
            throw new RuntimeException("Role Name " + request.getName() + " already exists!");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return RoleConvert.toRoleRes(this.roleRepository.save(role));
    }

    public RoleRes findById(long id) {
        var role = this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role ID", id));
        return RoleConvert.toRoleRes(role);
    }

    public void deleteById(long id) {
        var role = this.roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role ID", id));
        this.roleRepository.delete(role);
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
}
