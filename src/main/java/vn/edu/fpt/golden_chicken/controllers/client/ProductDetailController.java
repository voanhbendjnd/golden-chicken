package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductDetailController {
    ProductService productService;

    @GetMapping("/product/{id:[0-9]+}")
    public String detailPage(Model model, @PathVariable("id") Long id) {
        model.addAttribute("product", this.productService.findById(id));
        return "client/product-detail";
    }

}
