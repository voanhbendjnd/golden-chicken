package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.AddressServices;
import vn.edu.fpt.golden_chicken.services.ProductService;

@Controller
public class CheckoutController {
    private final ProductService productService; // Giả định Service lấy sản phẩm
    private final AddressServices addressServices;

    public CheckoutController(ProductService productService, AddressServices addressServices) {
        this.productService = productService;
        this.addressServices = addressServices;
    }

    @GetMapping("/checkout")
    public String handleCheckout(
            @RequestParam("id") long productId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            Model model) {

        ResProduct product = productService.findById(productId);

        var selectedAddress = (addressId != null)
                ? addressServices.findById(addressId)
                : addressServices.getDefaultAddress();

        if (product == null) {
            return "redirect:/home";
        }

        model.addAttribute("product", product);
        model.addAttribute("defaultAddress", selectedAddress);

        return "client/checkout";
    }

    @GetMapping("/checkout/addresses")
    public String listAddressCheckout(
            @RequestParam("productId") long productId,
            Model model) {

        var addresses = addressServices.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("productId", productId);

        return "client/address/listAddressCheckout";
    }


}