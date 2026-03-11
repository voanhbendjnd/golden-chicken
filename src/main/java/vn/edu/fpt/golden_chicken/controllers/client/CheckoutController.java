package vn.edu.fpt.golden_chicken.controllers.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
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
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.repositories.CustomerVoucherRepository;
import vn.edu.fpt.golden_chicken.repositories.UserRepository;
import vn.edu.fpt.golden_chicken.services.*;
import vn.edu.fpt.golden_chicken.services.kafka.RevenueService;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final UserRepository userRepository;
    private final ProductService productService;
    private final AddressServices addressServices;
    private final OrderService orderService;
    private final CartService cartService;
    private final RevenueService revenueService;
    // them moi
    private final ProfileService profileService;
    private final VoucherService voucherService;
    private final CustomerVoucherRepository customerVoucherRepository;

    public CheckoutController(ProductService productService, AddressServices addressServices,
                              OrderService orderService, CartService cartService, UserRepository userRepository,
                              RevenueService revenueService, ProfileService profileService, VoucherService voucherService,
                              CustomerVoucherRepository customerVoucherRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.addressServices = addressServices;
        this.cartService = cartService;
        this.revenueService = revenueService;
        this.profileService = profileService;
        this.voucherService = voucherService;
        this.customerVoucherRepository = customerVoucherRepository;
    }

    @GetMapping
    public String handleCheckout(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "voucherId", required = false) Long voucherId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            Model model) throws PermissionException {

        OrderDTO orderDTO = new OrderDTO();
        List<OrderDTO.OrderDetail> details = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        List<?> displayItems = new ArrayList<>();

        // =============================
        // CASE 1: BUY NOW
        // =============================
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

        // =============================
        // CASE 2: CART
        // =============================
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
                        x.getPrice().multiply(new BigDecimal(x.getQuantity()))
                );

                details.add(detail);
            }



            displayItems = cartItems;
            model.addAttribute("cartItems", cartItems);
        }

        else {
            return "redirect:/cart";
        }

        // =============================
        // ADDRESS
        // =============================

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

        // =============================
        // PRICE
        // =============================

        BigDecimal shippingFee = new BigDecimal("15000");

        orderDTO.setItems(details);
        orderDTO.setTotalProductPrice(totalPrice);
        orderDTO.setShippingFee(shippingFee);
        orderDTO.setDiscountAmount(BigDecimal.ZERO);
        orderDTO.setFinalAmount(totalPrice.add(shippingFee));
        orderDTO.setPaymentMethod(PaymentMethod.COD);

        // =============================
        // VOUCHER
        // =============================

        var currentUser = profileService.getCurrentUser();

        List<CustomerVoucher> vouchers =
                voucherService.getCustomerVouchers(currentUser.getId());

        model.addAttribute("vouchers", vouchers);

        if (voucherId != null) {

            CustomerVoucher selected =
                    customerVoucherRepository.findById(voucherId).orElse(null);

            if (selected != null) {

                Voucher voucher = selected.getVoucher();
                BigDecimal productPrice = orderDTO.getTotalProductPrice();

                if (voucher.getMinOrderValue() != null &&
                        productPrice.compareTo(voucher.getMinOrderValue()) < 0) {

                    model.addAttribute("voucherError",
                            "Đơn hàng chưa đủ điều kiện để dùng voucher này");

                } else {

                    BigDecimal discount = BigDecimal.ZERO;

                    if ("FIXED".equals(voucher.getDiscountType())) {

                        discount = BigDecimal.valueOf(voucher.getDiscountValue());

                    } else if ("PERCENT".equals(voucher.getDiscountType())) {

                        discount = productPrice
                                .multiply(BigDecimal.valueOf(voucher.getDiscountValue()))
                                .divide(BigDecimal.valueOf(100));
                    }

                    if (discount.compareTo(productPrice) > 0) {
                        discount = productPrice;
                    }

                    if ("SHIPPING".equals(voucher.getVoucherType())) {

                        BigDecimal shippingDiscount = discount;

                        if (shippingDiscount.compareTo(shippingFee) > 0) {
                            shippingDiscount = shippingFee;
                        }

                        orderDTO.setShippingFee(shippingFee.subtract(shippingDiscount));

                        BigDecimal finalAmount = productPrice
                                .add(orderDTO.getShippingFee());

                        orderDTO.setFinalAmount(finalAmount);

                    } else {

                        orderDTO.setDiscountAmount(discount);

                        BigDecimal finalAmount = productPrice
                                .add(orderDTO.getShippingFee())
                                .subtract(discount);

                        orderDTO.setFinalAmount(finalAmount);
                    }

                    model.addAttribute("selectedVoucher", selected);
                }
            }
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

        // KIỂM TRA ĐIỀU KIỆN ĐỂ TRẢ VỀ ĐÚNG GIAO DIỆN
        if (productId != null || (productIds != null && !productIds.isEmpty())) {
            // Trả về trang giao diện chọn địa chỉ cho Checkout (có nút Xác nhận địa chỉ)
            return "client/address/listAddressCheckout";
        }

        // Trả về trang quản lý địa chỉ cá nhân (hình số 6 bạn gửi)
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

    // chuyển sang trang chọn voucher
    @GetMapping("/vouchers")
    public String chooseVoucher(
            @RequestParam(value = "id", required = false) Long productId,
            @RequestParam(value = "ids", required = false) List<Long> productIds,
            Model model) {

        var currentUser = profileService.getCurrentUser();

        // Lấy danh sách voucher của khách hàng
        List<CustomerVoucher> vouchers =
                voucherService.getCustomerVouchers(currentUser.getId());

        model.addAttribute("vouchers", vouchers);

        // Truyền lại thông tin sản phẩm để khi chọn xong voucher biết đường quay về trang checkout đúng
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds);

        return "client/voucher-select";
    }
}