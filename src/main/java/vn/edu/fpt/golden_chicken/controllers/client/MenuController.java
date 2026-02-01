package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.response.CategoryProductsView;
import vn.edu.fpt.golden_chicken.domain.response.ResCategory;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.CategoryService;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
@RequiredArgsConstructor
public class MenuController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/menu")
    public String getMenuPage(Model model) {
        List<ResCategory> categories = categoryService.fetchAll();
        List<ResProduct> products = productService.getAllActiveForMenu();
        List<CategoryProductsView> categoryProductList = new ArrayList<>();
        for (ResCategory cat : categories) {
            List<ResProduct> list = products.stream()
                    .filter(p -> cat.getName() != null && cat.getName().equals(p.getCategoryName()))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                categoryProductList.add(new CategoryProductsView(cat.getName(), list));
            }
        }
        model.addAttribute("categoryProductList", categoryProductList);
        model.addAttribute("products", products != null ? products : List.of());
        return "client/menu";
    }
}
