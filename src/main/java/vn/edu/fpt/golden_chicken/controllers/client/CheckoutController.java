package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.golden_chicken.domain.entity.Product;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.AddressServices;
import vn.edu.fpt.golden_chicken.services.ProductService;

import static vn.edu.fpt.golden_chicken.domain.entity.ProductImage_.product;

@Controller
public class CheckoutController {
    private final ProductService productService; // Giả định Service lấy sản phẩm
    private final AddressServices addressServices;

    public CheckoutController(ProductService productService, AddressServices addressServices) {
        this.productService = productService;
        this.addressServices = addressServices;
    }

    @GetMapping("/checkout")
    public String handleCheckout(@RequestParam("id") long productId, Model model) {
        ResProduct selectedProduct = productService.findById(productId);
        var defaultAddress = addressServices.getDefaultAddress();
        var addresses = addressServices.getAllAddresses();
        if (selectedProduct != null) {
            model.addAttribute("defaultAddress", defaultAddress);
            model.addAttribute("product", selectedProduct);
            model.addAttribute("addresses", addresses);
        } else {
            return "redirect:/home";
        }
        return "client/checkout";
    }
}
