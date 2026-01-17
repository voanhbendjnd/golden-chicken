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

import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Category;
import vn.edu.fpt.golden_chicken.domain.request.CategoryDTO;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@Controller
@RequestMapping("/staff/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("")
    public String getTablePage(Model model,
            @Filter Specification<Category> spec,
            @PageableDefault(size = DefineVariable.pageSize, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        var res = this.categoryService.fetchWithPagination(spec, pageable);
        model.addAttribute("meta", res.getMeta());
        model.addAttribute("categories", res.getResult());
        return "staff/category/table";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("category", new CategoryDTO());
        return "staff/category/create";
    }

    @PostMapping("/create")
    public String create(Model model, @ModelAttribute("category") CategoryDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "staff/category/create";
        }
        try {
            this.categoryService.create(dto);
        } catch (DataInvalidException de) {
            model.addAttribute("errorMessage", de.getMessage());
            // bindingResult.rejectValue("error", de.getMessage());
            return "staff/category/create";
        }
        return "redirect:/staff/category";
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String updatePage(Model model, @PathVariable("id") long id) {
        model.addAttribute("category", this.categoryService.findById(id));
        return "staff/category/update";
    }

    @PostMapping("update")
    public String update(Model model, @ModelAttribute("category") CategoryDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "staff/category/update";
        }
        try {
            this.categoryService.update(dto);
        } catch (DataInvalidException de) {
            model.addAttribute("errorMessage", de.getMessage());

            // bindingResult.rejectValue("error", de.getMessage());
            return "staff/category/update";
        }
        return "redirect:/staff/category";
    }
}
