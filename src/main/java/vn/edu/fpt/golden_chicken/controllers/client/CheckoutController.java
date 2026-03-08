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

    @GetMapping("/order")
    public String handleOrderFromCart(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "addressId", required = false) Long addressId,
            Model model) throws PermissionException {
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
        for (var x : cartItems) {
            if (x.getQuantity() > 33 || x.getQuantity() < 1) {
                return "redirect:/cart";
            }
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

        // Lấy địa chỉ vừa chọn hoặc địa chỉ mặc định
        var selectedAddress = (addressId != null)
                ? addressServices.findById(addressId)
                : addressServices.getDefaultAddress();

        if (selectedAddress != null) {
            orderDTO.setName(selectedAddress.getRecipientName());
            orderDTO.setPhone(selectedAddress.getRecipientPhone());
            String fullAddress = String.format("%s, %s, %s",
                    selectedAddress.getSpecificAddress(),
                    selectedAddress.getWard(),
                    // selectedAddress.getDistrict(),
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
            @RequestParam(required = false) Long voucherId,
            @RequestParam(value = "addressId", required = false) Long addressId,
            Model model) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email != null) {
            var user = this.userRepository.findByEmail(email);
            if (user.getCustomer() == null) {
                return "client/auth/access-deny";
            }
        }
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
            String fullAddress = String.format("%s, %s, %s",
                    selectedAddress.getSpecificAddress(),
                    selectedAddress.getWard(),
                    // selectedAddress.getDistrict(),
                    selectedAddress.getCity());
            orderDTO.setAddress(fullAddress);
        }

        BigDecimal shippingFee = new BigDecimal("15000");
        orderDTO.setTotalProductPrice(product.getPrice());
        orderDTO.setShippingFee(shippingFee);
        orderDTO.setDiscountAmount(BigDecimal.ZERO);
        orderDTO.setFinalAmount(product.getPrice().add(shippingFee));
        orderDTO.setPaymentMethod(PaymentMethod.COD);

        model.addAttribute("order", orderDTO);
        model.addAttribute("product", product);
        model.addAttribute("defaultAddress", selectedAddress);
        // áp dụng voucher
        var currentUser = profileService.getCurrentUser();

        List<CustomerVoucher> vouchers =
                voucherService.getCustomerVouchers(currentUser.getId());

        model.addAttribute("vouchers", vouchers);
        // voucher đã chọn và xử lý voucher
        if (voucherId != null) {
            CustomerVoucher selected =
                    customerVoucherRepository.findById(voucherId).orElse(null);

            if (selected != null) {

                Voucher voucher = selected.getVoucher();
                BigDecimal productPrice = orderDTO.getTotalProductPrice();

                // kiểm tra điều kiện đơn tối thiểu
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

                    // Không cho giảm quá tiền sản phẩm
                    if (discount.compareTo(productPrice) > 0) {
                        discount = productPrice;
                    }


                    // xử lý loại voucher (PRODUCT / SHIPPING)
                    if ("SHIPPING".equals(voucher.getVoucherType())) {

                        BigDecimal shippingDiscount = discount;

                        // không giảm quá tiền ship
                        if (shippingDiscount.compareTo(shippingFee) > 0) {
                            shippingDiscount = shippingFee;
                        }

                        orderDTO.setShippingFee(shippingFee.subtract(shippingDiscount));

                        BigDecimal finalAmount = productPrice
                                .add(orderDTO.getShippingFee());

                        orderDTO.setFinalAmount(finalAmount);

                    } else {

                        // PRODUCT voucher
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


        return "client/checkout";
    }

    @GetMapping("/addresses")
    public String listAddressCheckout(
            @RequestParam(value = "productId", required = false) Long productId, // Cho Buy Now
            @RequestParam(value = "productIds", required = false) List<Long> productIds, // Cho Cart (nhiều món)
            Model model) {

        var addresses = addressServices.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("productId", productId);
        model.addAttribute("productIds", productIds); // Gửi danh sách đã chọn xuống

        return "client/address/listAddressCheckout";
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
            @RequestParam("id") Long productId,
            Model model) {

        var currentUser = profileService.getCurrentUser();

        List<CustomerVoucher> vouchers =
                voucherService.getCustomerVouchers(currentUser.getId());

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("productId", productId);

        return "client/voucher-select";
    }
}