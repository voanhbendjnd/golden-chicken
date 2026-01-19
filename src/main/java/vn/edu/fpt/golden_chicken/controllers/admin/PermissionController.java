package vn.edu.fpt.golden_chicken.controllers.admin;

import java.io.IOException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Permission;
import vn.edu.fpt.golden_chicken.domain.request.PermissionDTO;
import vn.edu.fpt.golden_chicken.domain.request.RoleDTO;
import vn.edu.fpt.golden_chicken.services.PermissionService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Controller
@RequestMapping("/admin/permission")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public String getTablePage(
            Model model,
            @Filter Specification<Permission> spec,
            @PageableDefault(size = DefineVariable.pageSize, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        var data = this.permissionService.fecthAllWithPaginationDTO(spec, pageable);
        model.addAttribute("permissions", data.getResult());
        model.addAttribute("meta", data.getMeta());
        return "admin/permission/table";
    }

    @GetMapping("/create")
    public String getCreatePage(Model model) {
        model.addAttribute("permission", new PermissionDTO());
        return "admin/permission/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("permission") @Valid PermissionDTO permissionDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/permission/create";
        }
        try {
            this.permissionService.create(permissionDTO);
            return "redirect:/admin/permission";
        } catch (DataInvalidException de) {
            bindingResult.rejectValue("name", "CONFLICT", de.getMessage());
            return "admin/permission/create";
        }
    }

    @GetMapping("/update")
    public String getUpdatePage(Model model, @PathVariable("id") long id) {
        model.addAttribute("permission", this.permissionService.findById(id));
        return "admin/permission/update";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("permission") @Valid PermissionDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/permission/update";
        }
        try {
            this.permissionService.update(dto);
            return "redirect:/admin/permission";
        } catch (DataInvalidException de) {
            bindingResult.rejectValue("name", "CONFLICT", de.getMessage());
            return "admin/permission/update";
        }
    }

    @PostMapping("/{id:[0-9]+}")
    public String delete(@PathVariable("id") long id) {
        this.permissionService.deleteById(id);
        return "redirect:/admin/permission";
    }

    @PostMapping("/import")
    public String importPermissions(@RequestParam("file") MultipartFile file,
            Model model,
            @PageableDefault(size = DefineVariable.pageSize, sort = "id", direction = Sort.Direction.DESC) Pageable pageable)
            throws IOException {
        try {
            this.permissionService.importPermissions(file);
            return "redirect:/admin/permission";
        } catch (DataInvalidException de) {
            model.addAttribute("errorMessage", de.getMessage());
            var data = this.permissionService.fecthAllWithPaginationDTO(Specification.where(null), pageable);
            model.addAttribute("permissions", data.getResult());
            model.addAttribute("meta", data.getMeta());
            return "admin/permission/table";
        }
    }
}
