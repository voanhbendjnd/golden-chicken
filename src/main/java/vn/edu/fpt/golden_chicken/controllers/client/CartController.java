package vn.edu.fpt.golden_chicken.controllers.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.edu.fpt.golden_chicken.domain.request.CartDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.services.CartService;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Controller
// @RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String cartPage(Model model) throws PermissionException {
        model.addAttribute("cart", this.cartService.getProductInCart());
        return "client/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody CartDTO dto) throws PermissionException {
        return this.cartService.addToCart(dto) != false ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.FORBIDDEN).body("Fail");
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<CartResponse> updateCart(@RequestBody CartDTO dto) throws PermissionException {
        this.cartService.updateQuantity(dto);
        CartResponse updatedCart = this.cartService.getProductInCart();
        return ResponseEntity.ok(updatedCart);
    }
}
