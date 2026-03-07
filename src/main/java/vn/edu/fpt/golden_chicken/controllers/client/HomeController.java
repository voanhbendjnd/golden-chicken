package vn.edu.fpt.golden_chicken.controllers.client;

import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({ "/", "/home" })
    public String getHomePage(Model model,
            @PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Filter Specification<Product> spec) {

        var data = this.productService.fetchAllWithPaginationHasCombo(pageable, spec);
        var originals = this.productService.fetchAllChickenHappy(spec, pageable);
        var saucies = this.productService.fetchAllChickenSauce(spec, pageable);
        var combos = this.productService.fetchAllComboWithPagination(spec, pageable);
        var deserts = this.productService.fetchAllLowMeal(spec, pageable);
        model.addAttribute("meta", data.getMeta());
        model.addAttribute("alls", data.getResult());
        model.addAttribute("originals", originals.getResult());
        model.addAttribute("deserts", deserts.getResult());
        model.addAttribute("saucies", saucies.getResult());
        model.addAttribute("combos", combos.getResult());
        return "client/home.djnd";
    }
}