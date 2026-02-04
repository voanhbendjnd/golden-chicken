package vn.edu.fpt.golden_chicken.controllers.staff;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.turkraft.springfilter.boot.Filter;

import vn.edu.fpt.golden_chicken.common.DefineVariable;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.repositories.ComboDetailRepository;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
@RequestMapping("/staff/combo")
public class ComboController {
    private final ProductService productService;
    private final ComboDetailRepository comboDetailRepository;
    private final CategoryService categoryService;

    public ComboController(CategoryService categoryService, ProductService productService,
            ComboDetailRepository comboDetailRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.comboDetailRepository = comboDetailRepository;
    }

    @GetMapping
    public String comboPage(Model model,
            @PageableDefault(size = DefineVariable.pageSize, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Filter Specification<Product> spec) {
        var data = this.productService.fetchAllComboWithPagination(spec, pageable);
        model.addAttribute("products", data.getResult());
        model.addAttribute("meta", data.getMeta());
        return "staff/combo/table";
    }

    @GetMapping("/update/{id:[0-9]+}")
    public String update(Model model, @PathVariable("id") Long id) {
        var combo = this.productService.findById(id);
        var comboDetails = this.comboDetailRepository.findByComboId(id);
        model.addAttribute("combo", combo);
        model.addAttribute("categories", this.categoryService.fetchAll());
        model.addAttribute("comboDetails", comboDetails);
        return "staff/combo/update";
    }
}
