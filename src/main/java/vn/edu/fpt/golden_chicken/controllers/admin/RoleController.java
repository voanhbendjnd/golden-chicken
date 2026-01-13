package vn.edu.fpt.golden_chicken.controllers.admin;

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

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.edu.fpt.golden_chicken.domain.entity.Role;
import vn.edu.fpt.golden_chicken.domain.request.RoleRequest;
import vn.edu.fpt.golden_chicken.services.RoleService;

@Controller
@RequestMapping("/admin/role")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public String listRole(Model model, @Filter Specification<Role> spec,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        var data = this.roleService.fetchAllWithPagination(spec, pageable);
        model.addAttribute("roles", data.getResult());
        model.addAttribute("meta", data.getMeta());
        return "admin/role/role-page-list";
    }

    @GetMapping("/create")
    public String getCreatePage(Model model) {
        model.addAttribute("newRole", new Role());
        return "admin/role/role-page-create";
    }

    @PostMapping("/create")
    public String createRole(@ModelAttribute("newRole") @Valid RoleRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "admin/role/role-page-create";
        }
        try {
            this.roleService.create(request);
        } catch (RuntimeException ex) {
            bindingResult.rejectValue("name", "CONFLICT", ex.getMessage());
            return "admin/role/role-page-create";
        }
        return "redirect:/admin/role";
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String getUpdataPage(Model model, @PathVariable("id") long id) {
        var role = this.roleService.findById(id);
        model.addAttribute("updateRole", role);
        return "admin/role/role-page-update";
    }

    @PostMapping("/update")
    public String updateRole(@ModelAttribute("updateRole") @Valid RoleRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/role/role-page-update";
        }
        try {
            this.roleService.update(request);
        } catch (RuntimeException ex) {
            bindingResult.rejectValue("name", "CONFLICT", ex.getMessage());
            return "admin/role/role-page-update";
        }
        return "redirect:/admin/role";

    }

    @PostMapping("/delete/{id:[0-9]+}")
    public String deleteRole(@PathVariable("id") long id) {
        this.roleService.deleteById(id);
        return "redirect:/admin/role";
    }
}
