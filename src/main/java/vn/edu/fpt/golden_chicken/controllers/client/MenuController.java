package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.turkraft.springfilter.boot.Filter;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.response.CategoryProductsView;
import vn.edu.fpt.golden_chicken.domain.response.ResCategory;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String getMenuPage(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var data = this.productService.fetchAllComboWithPagination(spec, pageable);
        List<ResCategory> categories = categoryService.fetchAll();
        List<ResProduct> products = productService.getAllActiveForMenu();
        List<CategoryProductsView> categoryProductList = new ArrayList<>();
        for (ResCategory cat : categories) {
            List<ResProduct> list = products.stream()
                    .filter(p -> cat.getName() != null && cat.getName().equals(p.getCategory().getName()))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                categoryProductList.add(new CategoryProductsView(cat.getName(), list));
            }
        }
        model.addAttribute("categoryProductList", categoryProductList);
        model.addAttribute("products", products != null ? products : List.of());
        return "client/menu";
    }

    @GetMapping("/ga-gion")
    public String getGaGon(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllChickenHappy(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Gà Giòn");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    @GetMapping("/ga-sot")
    public String getGaSot(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllChickenSauce(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Gà Sốt");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    @GetMapping("/combo")
    public String getCombo(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllComboWithPagination(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Combo");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }

    @GetMapping("/my")
    public String getMy(Model model, @Filter Specification<Product> spec,
            @PageableDefault(size = 20) Pageable pageable) {
        var views = new ArrayList<CategoryProductsView>();
        var data = this.productService.fetchAllNoodle(spec, pageable);
        var view = new CategoryProductsView();
        view.setCategoryName("Mỳ");
        view.setProducts(data.getResult());
        views.add(view);
        model.addAttribute("categoryProductList", views);
        // model.addAttribute("products", data.getResult());
        return "client/menu";
    }
}
