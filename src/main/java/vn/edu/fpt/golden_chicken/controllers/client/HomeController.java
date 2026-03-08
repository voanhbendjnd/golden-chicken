package vn.edu.fpt.golden_chicken.controllers.client;

import com.turkraft.springfilter.boot.Filter;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
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
        var newProducts = this.productService.fetchAllWithPaginationNewProduct(spec);
        var bestSeller = this.productService.fetchAllWithPaginationBestSeller(spec);
        model.addAttribute("meta", data.getMeta());
        model.addAttribute("alls", data.getResult());
        model.addAttribute("originals", originals.getResult());
        model.addAttribute("deserts", deserts.getResult());
        model.addAttribute("saucies", saucies.getResult());
        model.addAttribute("combos", combos.getResult());
        model.addAttribute("newProducts", newProducts.getResult());
        List<ResProduct> bestSellerLists = (List<ResProduct>) bestSeller.getResult();
        var bestFirst = bestSellerLists.subList(0, 6);
        var bestSecond = bestSellerLists.subList(6, bestSellerLists.size());
        model.addAttribute("firstBestSellers", bestFirst);

        model.addAttribute("secondBestSellers", bestSecond);
        return "client/home.djnd";
    }
}