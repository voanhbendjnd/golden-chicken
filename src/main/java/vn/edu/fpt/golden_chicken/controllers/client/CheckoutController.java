package vn.edu.fpt.golden_chicken.controllers.client;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.AddressServices;
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

    public CheckoutController(ProductService productService, AddressServices addressServices,
            OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
        this.addressServices = addressServices;
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

        // 1. Khởi tạo danh sách items và nạp sản phẩm đang mua vào
        OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
        detail.setProductId(product.getId());
        detail.setQuantity(1); // Mặc định mua 1
        orderDTO.setItems(List.of(detail));

        // 2. Nạp thông tin địa chỉ nếu có
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

        // 3. Nạp thông tin tiền bạc (Server tính toán sẵn)
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