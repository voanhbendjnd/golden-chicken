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
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.domain.response.CheckoutResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.repositories.CustomerVoucherRepository;
import vn.edu.fpt.golden_chicken.services.*;
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
    private final CustomerVoucherRepository customerVoucherRepository;
    private final CheckoutService checkoutService;

    public CheckoutController(ProductService productService, AddressServices addressServices,
            OrderService orderService, CartService cartService,
            RevenueService revenueService, ProfileService profileService, VoucherService voucherService,
            CustomerVoucherRepository customerVoucherRepository, CheckoutService checkoutService) {
        this.productService = productService;
        this.orderService = orderService;
        this.addressServices = addressServices;
        this.cartService = cartService;
        this.revenueService = revenueService;
        this.profileService = profileService;
        this.voucherService = voucherService;
        this.customerVoucherRepository = customerVoucherRepository;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public String handleCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "voucherId", required = false) Long voucherId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            Model model) throws PermissionException {

        CheckoutResponse response =
                checkoutService.buildCheckout(productId, ids, voucherId, addressId);

        if (response.getRedirect() != null) {
            return response.getRedirect();
        }

        model.addAllAttributes(response.getModel());

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
            Model model) {

        var currentUser = profileService.getCurrentUser();

        List<CustomerVoucher> vouchers = voucherService.getCustomerVouchers(currentUser.getId());

        model.addAttribute("vouchers", vouchers);

        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);

        return "client/voucher-select";
    }
}