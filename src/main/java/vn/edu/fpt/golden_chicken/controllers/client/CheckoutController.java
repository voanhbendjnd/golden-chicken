package vn.edu.fpt.golden_chicken.controllers.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CheckoutResponse;
import vn.edu.fpt.golden_chicken.services.*;
import vn.edu.fpt.golden_chicken.services.kafka.RevenueService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckoutController {
    AddressServices addressServices;
    OrderService orderService;
    RevenueService revenueService;
    ProfileService profileService;
    VoucherService voucherService;
    CheckoutService checkoutService;
    UserService userService;

    @GetMapping
    public String handleCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Model model) throws PermissionException {
        var user = this.userService.getUserInContext();
        if (user.getCustomer() == null) {
            throw new PermissionException("You do not have permission!");
        }

        System.out.println(">>> handleCheckout params: orderId=" + orderId + ", productId=" + productId + ", ids=" + ids);
        if(quantity == null || quantity <= 0) {
            quantity = 1;
            return handleCheckout(productId, ids, orderId, productVoucherId, shippingVoucherId,
                    addressId, quantity, model);
        }
        CheckoutResponse response = checkoutService.buildCheckout(productId, ids, orderId, productVoucherId,
                shippingVoucherId, addressId, quantity);

        if (response.getRedirect() != null) {
            return response.getRedirect();
        }

        model.addAllAttributes(response.getModel());
        model.addAttribute("orderId", orderId);

        return "client/checkout";
    }

    @GetMapping("/addresses")
    public String listAddressCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            Model model) {

        var addresses = addressServices.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);

        if (productId != null || (productIds != null && !productIds.isEmpty()) || orderId != null) {
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
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "addressId", required = false) Long addressId,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model) throws PermissionException {

        var currentUser = profileService.getCurrentUser();
        List<CustomerVoucher> vouchers = voucherService.getCustomerVouchers(currentUser.getId());

        BigDecimal orderTotal = checkoutService.calculateOrderTotal(productId, productIds, quantity);
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
        model.addAttribute("quantity", quantity);
        model.addAttribute("addressId", addressId);
        model.addAttribute("orderId", orderId);

        return "client/voucher-select";
    }

    @PostMapping("/apply-vouchers")
    public String applyVouchers(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "addressId", required = false) Long addressId,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "voucherIds", required = false) List<Long> voucherIds,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Model model) throws PermissionException {
        OrderDTO selection;
        try {
            selection = voucherService.resolveVoucherSelection(profileService.getCurrentUser(), voucherIds,
                    voucherCode);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("voucherError", ex.getMessage());
            return handleCheckout(productId, ids, orderId, null, null, addressId, quantity, model);
        }

        if (selection.getProductVoucherId() == null && selection.getShippingVoucherId() == null) {
            return handleCheckout(productId, ids, orderId, null, null, addressId, quantity, model);
        }

        return handleCheckout(productId, ids, orderId, selection.getProductVoucherId(), selection.getShippingVoucherId(),
                addressId, quantity, model);
    }
}
