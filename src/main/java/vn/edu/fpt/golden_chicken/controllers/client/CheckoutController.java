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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CheckoutResponse;
import vn.edu.fpt.golden_chicken.services.AddressServices;
import vn.edu.fpt.golden_chicken.services.CheckoutService;
import vn.edu.fpt.golden_chicken.services.OrderService;
import vn.edu.fpt.golden_chicken.services.ProductService;
import vn.edu.fpt.golden_chicken.services.ProfileService;
import vn.edu.fpt.golden_chicken.services.UserService;
import vn.edu.fpt.golden_chicken.services.VoucherService;
import vn.edu.fpt.golden_chicken.services.kafka.RevenueService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

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
    ProductService productService;

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

        if (productId == null && model.containsAttribute("productId") && model.asMap().get("productId") != null) {
            productId = Long.valueOf(model.asMap().get("productId").toString());
        }
        if (ids == null && model.containsAttribute("ids") && model.asMap().get("ids") != null) {
            ids = (List<Long>) model.asMap().get("ids");
        }
        if (orderId == null && model.containsAttribute("orderId") && model.asMap().get("orderId") != null) {
            orderId = Long.valueOf(model.asMap().get("orderId").toString());
        }
        if (quantity == null && model.containsAttribute("quantity") && model.asMap().get("quantity") != null) {
            quantity = Integer.valueOf(model.asMap().get("quantity").toString());
        }
        if (addressId == null && model.containsAttribute("addressId") && model.asMap().get("addressId") != null) {
            addressId = Long.valueOf(model.asMap().get("addressId").toString());
        }
        if (productVoucherId == null && model.containsAttribute("productVoucherId")
                && model.asMap().get("productVoucherId") != null) {
            productVoucherId = Long.valueOf(model.asMap().get("productVoucherId").toString());
        }
        if (shippingVoucherId == null && model.containsAttribute("shippingVoucherId")
                && model.asMap().get("shippingVoucherId") != null) {
            shippingVoucherId = Long.valueOf(model.asMap().get("shippingVoucherId").toString());
        }

        var user = this.userService.getUserInContext();
        if (user.getCustomer() == null) {
            throw new PermissionException("You do not have permission!");
        }

        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        if (productId != null && !productService.checkProductAndCategoryActive(productId)) {
            return "redirect:/home";
        }
        if (orderId != null) {
            if (!this.orderService.checkOrderByCustomer(orderId))
                return "redirect:/home";
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

    @PostMapping
    public String handleCheckoutPost(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            @RequestParam(value = "clearVouchers", required = false) Boolean clearVouchers,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("productId", productId);
        redirectAttributes.addFlashAttribute("ids", ids);
        redirectAttributes.addFlashAttribute("orderId", orderId);
        if (clearVouchers == null || !clearVouchers) {
            redirectAttributes.addFlashAttribute("productVoucherId", productVoucherId);
            redirectAttributes.addFlashAttribute("shippingVoucherId", shippingVoucherId);
        }
        redirectAttributes.addFlashAttribute("addressId", addressId);
        redirectAttributes.addFlashAttribute("quantity", quantity);

        return "redirect:/checkout";
    }

    @GetMapping("/addresses")
    public String listAddressCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Model model) {

        if (productId == null && model.containsAttribute("productId") && model.asMap().get("productId") != null) {
            productId = Long.valueOf(model.asMap().get("productId").toString());
        }
        if (productIds == null && model.containsAttribute("productIds") && model.asMap().get("productIds") != null) {
            productIds = model.asMap().get("productIds").toString();
        }
        if (orderId == null && model.containsAttribute("orderId") && model.asMap().get("orderId") != null) {
            orderId = Long.valueOf(model.asMap().get("orderId").toString());
        }
        if (quantity == null && model.containsAttribute("quantity") && model.asMap().get("quantity") != null) {
            quantity = Integer.valueOf(model.asMap().get("quantity").toString());
        }
        if (productVoucherId == null && model.containsAttribute("productVoucherId")
                && model.asMap().get("productVoucherId") != null) {
            productVoucherId = Long.valueOf(model.asMap().get("productVoucherId").toString());
        }
        if (shippingVoucherId == null && model.containsAttribute("shippingVoucherId")
                && model.asMap().get("shippingVoucherId") != null) {
            shippingVoucherId = Long.valueOf(model.asMap().get("shippingVoucherId").toString());
        }

        var addresses = addressServices.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);
        model.addAttribute("orderId", orderId);
        model.addAttribute("productVoucherId", productVoucherId);
        model.addAttribute("shippingVoucherId", shippingVoucherId);
        model.addAttribute("quantity", quantity);

        // var defaultAddress = addressServices.getDefaultAddress();
        // List<ResAddress> additionalAddresses = new ArrayList<>();
        // if (addresses != null) {
        // for (ResAddress a : addresses) {
        // if (a.getIsDefault() == null || !a.getIsDefault()) {
        // additionalAddresses.add(a);
        // }
        // }
        // }
        // model.addAttribute("additionalAddresses", additionalAddresses);
        // model.addAttribute("defaultAddress", defaultAddress);

        // if (productId != null || (productIds != null && !productIds.isEmpty()) ||
        // orderId != null) {
        return "client/address/listAddressCheckout";
        // }

        // return "client/address/listAddress";
    }

    @PostMapping("/addresses")
    public String listAddressCheckoutPost(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "productIds", required = false) String productIds,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "productVoucherId", required = false) Long productVoucherId,
            @RequestParam(value = "shippingVoucherId", required = false) Long shippingVoucherId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("productId", productId);
        redirectAttributes.addFlashAttribute("productIds", productIds);
        redirectAttributes.addFlashAttribute("orderId", orderId);
        redirectAttributes.addFlashAttribute("productVoucherId", productVoucherId);
        redirectAttributes.addFlashAttribute("shippingVoucherId", shippingVoucherId);
        redirectAttributes.addFlashAttribute("quantity", quantity);

        return "redirect:/checkout/addresses";
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

        if (productId == null && model.containsAttribute("id")) {
            productId = (Long) model.asMap().get("id");
        }
        if (productIds == null && model.containsAttribute("ids")) {
            productIds = (List<Long>) model.asMap().get("ids");
        }
        if (quantity == null && model.containsAttribute("quantity")) {
            quantity = (Integer) model.asMap().get("quantity");
        }
        if (addressId == null && model.containsAttribute("addressId")) {
            addressId = (Long) model.asMap().get("addressId");
        }
        if (orderId == null && model.containsAttribute("orderId")) {
            orderId = (Long) model.asMap().get("orderId");
        }

        var currentUser = profileService.getCurrentUser();
        List<CustomerVoucher> vouchers = voucherService.getCustomerVouchers(currentUser.getId());

        BigDecimal orderTotal = checkoutService.calculateOrderTotal(productId, productIds, quantity, orderId);
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

    @PostMapping("/vouchers")
    public String listVouchersPost(
            @RequestParam(value = "id", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> productIds,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "addressId", required = false) Long addressId,
            @RequestParam(value = "orderId", required = false) Long orderId,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("id", productId);
        redirectAttributes.addFlashAttribute("ids", productIds);
        redirectAttributes.addFlashAttribute("quantity", quantity);
        redirectAttributes.addFlashAttribute("addressId", addressId);
        redirectAttributes.addFlashAttribute("orderId", orderId);

        return "redirect:/checkout/vouchers";
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

        return handleCheckout(productId, ids, orderId, selection.getProductVoucherId(),
                selection.getShippingVoucherId(),
                addressId, quantity, model);
    }
}
