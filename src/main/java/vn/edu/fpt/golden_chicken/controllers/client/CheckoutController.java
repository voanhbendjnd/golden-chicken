package vn.edu.fpt.golden_chicken.controllers.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.services.AddressServices;
import vn.edu.fpt.golden_chicken.services.CartService;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.ProductService;
import vn.edu.fpt.golden_chicken.services.ProfileService;
import vn.edu.fpt.golden_chicken.services.VoucherService;
import vn.edu.fpt.golden_chicken.services.kafka.RevenueService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final ProductService productService;
    private final AddressServices addressServices;
    private final OrderService orderService;
    private final CartService cartService;
    private final RevenueService revenueService;
    private final ProfileService profileService;
    private final VoucherService voucherService;

    public CheckoutController(ProductService productService, AddressServices addressServices,
            OrderService orderService, CartService cartService,
            RevenueService revenueService, ProfileService profileService, VoucherService voucherService) {
        this.productService = productService;
        this.orderService = orderService;
        this.addressServices = addressServices;
        this.cartService = cartService;
        this.revenueService = revenueService;
        this.profileService = profileService;
        this.voucherService = voucherService;
    }

    @GetMapping
    public String handleCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            Model model) throws PermissionException {

        OrderDTO orderDTO = new OrderDTO();
        List<OrderDTO.OrderDetail> details = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (productId != null) {

            ResProduct product = productService.findById(productId);
            if (product == null) {
                return "redirect:/home";
            }

            OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
            detail.setProductId(product.getId());
            detail.setQuantity(1);

            details.add(detail);

            totalPrice = product.getPrice();

            model.addAttribute("product", product);
        }

        else if (ids != null && !ids.isEmpty()) {

            CartResponse response = cartService.getProductInCart();

            if (response.getItems() == null) {
                return "redirect:/cart";
            }

            var cartItems = response.getItems().stream()
                    .filter(item -> ids.contains(item.getProductId()))
                    .toList();

            if (cartItems.isEmpty()) {
                return "redirect:/cart";
            }

            for (var x : cartItems) {

                if (x.getQuantity() > 33 || x.getQuantity() < 1) {
                    return "redirect:/cart";
                }

                OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
                detail.setItemId(x.getItemId());
                detail.setProductId(x.getProductId());
                detail.setQuantity(x.getQuantity());

                totalPrice = totalPrice.add(
                        x.getPrice().multiply(new BigDecimal(x.getQuantity())));

                details.add(detail);
            }

            model.addAttribute("cartItems", cartItems);
        }

        else {
            return "redirect:/cart";
        }

        var selectedAddress = (addressId != null)
                ? addressServices.findById(addressId)
                : addressServices.getDefaultAddress();
        if (selectedAddress != null) {

            orderDTO.setName(selectedAddress.getRecipientName());
            orderDTO.setPhone(selectedAddress.getRecipientPhone());

            String fullAddress = String.format("%s, %s, %s",
                    selectedAddress.getSpecificAddress(),
                    selectedAddress.getWard(),
                    selectedAddress.getCity());

            orderDTO.setAddress(fullAddress);
        }

        BigDecimal shippingFee = new BigDecimal("15000");

        orderDTO.setItems(details);
        orderDTO.setTotalProductPrice(totalPrice);
        orderDTO.setShippingFee(shippingFee);
        orderDTO.setDiscountAmount(BigDecimal.ZERO);
        orderDTO.setFinalAmount(totalPrice.add(shippingFee));
        orderDTO.setPaymentMethod(PaymentMethod.COD);

        var currentUser = profileService.getCurrentUser();

        List<CustomerVoucher> vouchers = voucherService.getCustomerVouchers(currentUser.getId());

        model.addAttribute("vouchers", vouchers);

        if (productVoucherId != null || shippingVoucherId != null) {
            voucherService.applyVouchersToOrder(productVoucherId, shippingVoucherId, orderDTO, shippingFee, model);
            orderDTO.setProductVoucherId(productVoucherId);
            orderDTO.setShippingVoucherId(shippingVoucherId);
        }

        model.addAttribute("order", orderDTO);
        model.addAttribute("defaultAddress", selectedAddress);

        return "client/checkout";
    }


    @GetMapping("/addresses")
    public String listAddressCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            Model model) {

        var addresses = addressServices.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);

        if (productId != null || (productIds != null && !productIds.isEmpty())) {
            return "client/address/listAddressCheckout";
        }

        return "client/address/addressBook";
    }

    @PostMapping("/order")
    public String order(@ModelAttribute("order") OrderDTO dto) throws PermissionException {
        if (dto.getProductVoucherId() == null && dto.getShippingVoucherId() == null) {
            dto.setDiscountAmount(BigDecimal.ZERO);
        }
        var order = this.orderService.order(dto);

        if (dto.getPaymentMethod() == PaymentMethod.VNPAY) {
            return "redirect:/payment/create?orderId=" + order.getId();
        }
        System.out.println(">>> TOTAL AMOUNT:" + this.revenueService.getTotalRevenue());
        return "redirect:/checkout/payment-success";
    }

    @GetMapping("/revenue")
    public String testPage() {
        System.out.println(">>> Revenue: " + this.revenueService.getTotalRevenue());
        return "redirect:/login";
    }

    @GetMapping("/payment-success")
    public String successPage() {
        return "client/payment/payment.success";
    }

    @GetMapping("/vouchers")
    public String chooseVoucher(
            @RequestParam(value = "id", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> productIds,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model) throws PermissionException {

        var currentUser = profileService.getCurrentUser();
        List<CustomerVoucher> vouchers = voucherService.getCustomerVouchers(currentUser.getId());

        BigDecimal orderTotal = calculateOrderTotal(productId, productIds);
        final BigDecimal orderTotalFinal = orderTotal;

        List<CustomerVoucher> allVouchers = vouchers;

        int total = allVouchers.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int currentPage = Math.max(1, Math.min(page, Math.max(totalPages, 1)));
        int fromIndex = Math.max(0, (currentPage - 1) * size);
        int toIndex = Math.min(fromIndex + size, total);
        List<CustomerVoucher> pageVouchers = allVouchers.subList(fromIndex, toIndex);

        model.addAttribute("vouchers", pageVouchers);
        model.addAttribute("orderTotal", orderTotalFinal);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);

        return "client/voucher-select";
    }

    private BigDecimal calculateOrderTotal(Long productId, List<Long> productIds) throws PermissionException {
        if (productId != null) {
            var product = productService.findById(productId);
            return product != null ? product.getPrice() : BigDecimal.ZERO;
        }
        if (productIds != null && !productIds.isEmpty()) {
            var cart = cartService.getProductInCart();
            BigDecimal total = BigDecimal.ZERO;
            if (cart.getItems() != null) {
                for (var item : cart.getItems()) {
                    if (productIds.contains(item.getProductId())) {
                        total = total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    }
                }
            }
            return total;
        }
        return BigDecimal.ZERO;
    }

    @PostMapping("/apply-vouchers")
    public String applyVouchers(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "voucherIds", required = false) List<Long> voucherIds,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            Model model) throws PermissionException {
        var selection = voucherService.resolveVoucherSelection(profileService.getCurrentUser(), voucherIds, voucherCode,
                model);
        if (!selection.isValid()) {
            return handleCheckout(productId, ids, null, null, null, model);
        }

        return handleCheckout(productId, ids, selection.getProductVoucherId(), selection.getShippingVoucherId(), null,
                model);
    }
}