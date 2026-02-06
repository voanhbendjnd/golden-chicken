package vn.edu.fpt.golden_chicken.controllers.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;

import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.User;
import vn.edu.fpt.golden_chicken.domain.request.UserDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResRole;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.RoleService;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;
import vn.edu.fpt.golden_chicken.utils.exceptions.EmailAlreadyExistsException;

@Controller
@RequestMapping("/admin/user")
public class UserController {
    private final RoleService roleService;
    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository, RoleService roleService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @ModelAttribute("roles")
    public List<ResRole> getAllRoles() {
        return this.roleService.fetchAll();
    }

    @GetMapping("")
    public String listUsers(Model model,
            @RequestParam(required = false) String fullName,
            @Filter Specification<User> spec,
            @PageableDefault(size = DefineVariable.pageSize, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // If fullName parameter is provided, create a filter specification
        if (fullName != null && !fullName.trim().isEmpty()) {
            spec = (root, query, criteriaBuilder) -> criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")),
                    "%" + fullName.toLowerCase().trim() + "%");
        }

        var data = userService.fetchAllWithPagination(pageable, spec);
        model.addAttribute("users", data.getResult());
        model.addAttribute("meta", data.getMeta());
        return "admin/user/table";
    }

    @GetMapping("/create")
    public String createUserPage(Model model) {
        model.addAttribute("newUser", new UserDTO());
        // model.addAttribute("roles", this.roleService.fetchAll());
        return "admin/user/create"; // dẫn chính xác tới folder + file create.jsp
    }

    @PostMapping("/create")
    public String create(Model model, @ModelAttribute("newUser") @Valid UserDTO request,
            BindingResult bindingResult) {
        if (this.userRepository.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
        }
        if (bindingResult.hasErrors()) {
            return "admin/user/create";
        }

        this.userService.create(request);
        return "redirect:/admin/user"; // redirect tới mapping
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String updateUserPage(Model model, @PathVariable("id") Long id) {
        var userUpdate = this.userService.findById(id);
        model.addAttribute("updateUser", userUpdate);
        return "admin/user/update";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("updateUser") @Valid UserDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/user/update";
        }
        try {
            this.userService.update(request);
        } catch (EmailAlreadyExistsException ex) {
            bindingResult.rejectValue("email", "error.user", ex.getMessage());
            return "admin/user/update";
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/{id:[0-9]+}")
    public String fetchUser(Model model, @PathVariable("id") long id) {
        model.addAttribute("userData", this.userService.findById(id));
        return "admin/user/detail";
    }

    @PostMapping("/delete/{id:[0-9]+}")
    public String delete(@PathVariable("id") long id) {
        this.userService.deleteById(id);
        return "redirect:/admin/user";
    }

    // @DeleteMapping("/{id:[0-9]+}")
    // @ResponseBody
    // public ResponseEntity<?> delete(@PathVariable("id") Long id) {
    // try {
    // this.userService.deleteById(id);
    // return ResponseEntity.ok("Delete User Success!");
    // } catch (Exception ex) {
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    // }
    // }

    @PostMapping("/import")
    public String importUser(@RequestParam("file") MultipartFile file, Model model,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable)
            throws IOException {
        try {
            this.userService.importUsers(file);
            return "redirect:/admin/user";
        } catch (EmailAlreadyExistsException ee) {
            model.addAttribute("errorMessage", ee.getMessage());
            var data = userService.fetchAllWithPagination(pageable, Specification.where(null));
            model.addAttribute("users", data.getResult());
            model.addAttribute("meta", data.getMeta());
            return "admin/user/table";
        } catch (IllegalArgumentException ia) {
            model.addAttribute("errorMessage", ia.getMessage());
            var data = userService.fetchAllWithPagination(pageable, Specification.where(null));
            model.addAttribute("users", data.getResult());
            model.addAttribute("meta", data.getMeta());
            return "admin/user/table";
        } catch (DataInvalidException de) {
            model.addAttribute("errorMessage", de.getMessage());
            var data = userService.fetchAllWithPagination(pageable, Specification.where(null));
            model.addAttribute("users", data.getResult());
            model.addAttribute("meta", data.getMeta());
            return "admin/user/table";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            var data = userService.fetchAllWithPagination(pageable, Specification.where(null));
            model.addAttribute("users", data.getResult());
            model.addAttribute("meta", data.getMeta());
            return "admin/user/table";
        }
    }

}
