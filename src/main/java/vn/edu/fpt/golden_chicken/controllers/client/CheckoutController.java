package vn.edu.fpt.golden_chicken.controllers.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.AddressServices;
import vn.edu.fpt.golden_chicken.services.CartService;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.ProductService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final ProductService productService;
    private final AddressServices addressServices;
    private final OrderService orderService;
    private final CartService cartService;

    public CheckoutController(ProductService productService, AddressServices addressServices,
            OrderService orderService, CartService cartService) {
        this.productService = productService;
        this.orderService = orderService;
        this.addressServices = addressServices;
        this.cartService = cartService;
    }

    @GetMapping("/order")
    public String handleOrderFromCart(
            @RequestParam(value = "ids", required = false) List<Long> ids, Model model) throws PermissionException {
        var orderDTO = new OrderDTO();
        var details = new ArrayList<OrderDTO.OrderDetail>();

        CartResponse response = this.cartService.getProductInCart();
        if (response.getItems() == null) {
            return "redirect:/cart";
        }

        var cartItems = response.getItems().stream()
                .filter(item -> ids == null || ids.contains(item.getProductId()))
                .toList();

        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (var x : cartItems) {
            var detail = new OrderDTO.OrderDetail();
            detail.setItemId(x.getItemId());
            detail.setProductId(x.getProductId());
            detail.setQuantity(x.getQuantity());

            totalPrice = totalPrice.add(x.getPrice().multiply(new BigDecimal(x.getQuantity())));
            details.add(detail);
        }

        BigDecimal shippingFee = new BigDecimal("15000");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal finalAmount = totalPrice.add(shippingFee).subtract(discount);

        orderDTO.setItems(details);
        orderDTO.setTotalProductPrice(totalPrice);
        orderDTO.setShippingFee(shippingFee);
        orderDTO.setDiscountAmount(discount);
        orderDTO.setFinalAmount(finalAmount);
        orderDTO.setPaymentMethod(PaymentMethod.COD);

        var selectedAddress = addressServices.getDefaultAddress();
        if (selectedAddress != null) {
            orderDTO.setName(selectedAddress.getRecipientName());
            orderDTO.setPhone(selectedAddress.getRecipientPhone());
            String fullAddress = String.format("%s, %s, %s, %s",
                    selectedAddress.getSpecificAddress(),
                    selectedAddress.getWard(),
                    selectedAddress.getDistrict(),
                    selectedAddress.getCity());
            orderDTO.setAddress(fullAddress);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("order", orderDTO);
        model.addAttribute("defaultAddress", selectedAddress);
        return "client/checkout";
    }

    @GetMapping
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
        OrderDTO orderDTO = new OrderDTO();

        OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
        detail.setProductId(product.getId());
        detail.setQuantity(1);
        orderDTO.setItems(List.of(detail));

        if (selectedAddress != null) {
            orderDTO.setName(selectedAddress.getRecipientName());
            orderDTO.setPhone(selectedAddress.getRecipientPhone());
            String fullAddress = String.format("%s, %s, %s, %s",
                    selectedAddress.getSpecificAddress(),
                    selectedAddress.getWard(),
                    selectedAddress.getDistrict(),
                    selectedAddress.getCity());
            orderDTO.setAddress(fullAddress);
        }

        BigDecimal shippingFee = new BigDecimal("15000");
        orderDTO.setTotalProductPrice(product.getPrice());
        orderDTO.setShippingFee(shippingFee);
        orderDTO.setDiscountAmount(BigDecimal.ZERO);
        orderDTO.setFinalAmount(product.getPrice().add(shippingFee));
        orderDTO.setPaymentMethod(PaymentMethod.COD); // Mặc định phương th
        model.addAttribute("order", orderDTO);
        model.addAttribute("product", product);
        model.addAttribute("defaultAddress", selectedAddress);

        return "client/checkout";
    }

    @GetMapping("/addresses")
    public String listAddressCheckout(
            @RequestParam("productId") long productId,
            Model model) {

        var addresses = addressServices.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("productId", productId);

        return "client/address/listAddressCheckout";
    }

    @PostMapping("/order")
    public String order(@ModelAttribute("order") OrderDTO dto) throws PermissionException {
        this.orderService.order(dto);
        return "redirect:/";
    }

}