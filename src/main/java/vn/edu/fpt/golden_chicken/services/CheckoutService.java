package vn.edu.fpt.golden_chicken.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.domain.entity.CustomerVoucher;
import vn.edu.fpt.golden_chicken.domain.entity.Voucher;
import vn.edu.fpt.golden_chicken.domain.request.OrderDTO;
import vn.edu.fpt.golden_chicken.domain.response.CartResponse;
import vn.edu.fpt.golden_chicken.domain.response.CheckoutResponse;
import vn.edu.fpt.golden_chicken.domain.response.ResOrder;
import vn.edu.fpt.golden_chicken.domain.response.ResProduct;
import vn.edu.fpt.golden_chicken.repositories.CustomerVoucherRepository;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;
import vn.edu.fpt.golden_chicken.utils.exceptions.PermissionException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckoutService {
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final AddressServices addressServices;
    private final VoucherService voucherService;
    private final ProfileService profileService;
    private final CustomerVoucherRepository customerVoucherRepository;

    public CheckoutResponse buildCheckout(
            Long productId,
            List<Long> ids,
            Long orderId,
            Long voucherId,
            Long productVoucherId,
            Long shippingVoucherId,
            Long addressId) throws PermissionException {

        CheckoutResponse response = new CheckoutResponse();

        OrderDTO orderDTO = new OrderDTO();
        List<OrderDTO.OrderDetail> details = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        Map<String, Object> model = response.getModel();

        if (orderId != null) {

            ResOrder resOrder = orderService.findById(orderId);
            var currentUser = profileService.getCurrentUser();
            if (currentUser == null || currentUser.getCustomer() == null) {
                response.setRedirect("redirect:/login");
                return response;
            }
            if (resOrder.getCustomerId() == null
                    || !resOrder.getCustomerId().equals(currentUser.getCustomer().getId())) {
                response.setRedirect("redirect:/order-history");
                return response;
            }
            if (resOrder.getStatus() != OrderStatus.COMPLETED && resOrder.getStatus() != OrderStatus.DELIVERED) {
                response.setRedirect("redirect:/order-history");
                return response;
            }

            List<CartResponse.CartItemDTO> cartItemsForDisplay = new ArrayList<>();
            for (var item : resOrder.getItems()) {
                OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
                detail.setProductId(item.getProductId());
                detail.setQuantity(item.getQuantity());
                details.add(detail);
                BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalPrice = totalPrice.add(subTotal);

                CartResponse.CartItemDTO displayItem = new CartResponse.CartItemDTO();
                displayItem.setProductId(item.getProductId());
                displayItem.setProductName(item.getName());
                displayItem.setProductImg(item.getImg());
                displayItem.setQuantity(item.getQuantity());
                displayItem.setPrice(item.getPrice());
                displayItem.setSubTotal(subTotal);
                cartItemsForDisplay.add(displayItem);
            }
            model.put("cartItems", cartItemsForDisplay);
        } else if (productId != null) {

            ResProduct product = productService.findById(productId);

            if (product == null) {
                response.setRedirect("redirect:/home");
                return response;
            }

            OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
            detail.setProductId(product.getId());
            detail.setQuantity(1);

            details.add(detail);

            totalPrice = product.getPrice();

            model.put("product", product);
        }

        else if (ids != null && !ids.isEmpty()) {

            CartResponse cart = cartService.getProductInCart();

            if (cart.getItems() == null) {
                response.setRedirect("redirect:/cart");
                return response;
            }

            var cartItems = cart.getItems().stream()
                    .filter(item -> ids.contains(item.getProductId()))
                    .toList();

            if (cartItems.isEmpty()) {
                response.setRedirect("redirect:/cart");
                return response;
            }

            for (var item : cartItems) {

                if (item.getQuantity() > 33 || item.getQuantity() < 1) {
                    response.setRedirect("redirect:/cart");
                    return response;
                }

                OrderDTO.OrderDetail detail = new OrderDTO.OrderDetail();
                detail.setItemId(item.getItemId());
                detail.setProductId(item.getProductId());
                detail.setQuantity(item.getQuantity());

                totalPrice = totalPrice.add(
                        item.getPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())));

                details.add(detail);
            }

            model.put("cartItems", cartItems);
        }

        else {
            response.setRedirect("redirect:/cart");
            return response;
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

        model.put("vouchers", vouchers);

        Long resolvedProductVoucherId = productVoucherId;
        Long resolvedShippingVoucherId = shippingVoucherId;
        if (resolvedProductVoucherId == null && resolvedShippingVoucherId == null && voucherId != null) {
            var selected = customerVoucherRepository.findById(voucherId).orElse(null);
            if (selected != null && selected.getVoucher() != null) {
                var voucherType = selected.getVoucher().getVoucherType();
                if ("PRODUCT".equalsIgnoreCase(voucherType)) {
                    resolvedProductVoucherId = voucherId;
                } else if ("SHIPPING".equalsIgnoreCase(voucherType)) {
                    resolvedShippingVoucherId = voucherId;
                }
            }
        }

        applyVouchers(resolvedProductVoucherId, resolvedShippingVoucherId, orderDTO, shippingFee, model);

        model.put("order", orderDTO);
        model.put("defaultAddress", selectedAddress);

        return response;
    }

    private void applyVouchers(Long productVoucherId,
            Long shippingVoucherId,
            OrderDTO orderDTO,
            BigDecimal shippingFee,
            Map<String, Object> model) {

        BigDecimal productPrice = orderDTO.getTotalProductPrice();
        BigDecimal productDiscount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;

        CustomerVoucher selectedProductVoucher = null;
        CustomerVoucher selectedShippingVoucher = null;

        if (productVoucherId != null) {
            selectedProductVoucher = customerVoucherRepository.findById(productVoucherId).orElse(null);
        }
        if (shippingVoucherId != null) {
            selectedShippingVoucher = customerVoucherRepository.findById(shippingVoucherId).orElse(null);
        }

        if (selectedProductVoucher != null && selectedProductVoucher.getVoucher() != null) {
            BigDecimal discount = calculateDiscount(selectedProductVoucher.getVoucher(), productPrice, productPrice, model);
            if (discount != null) {
                productDiscount = discount;
            }
        }

        if (selectedShippingVoucher != null && selectedShippingVoucher.getVoucher() != null) {
            BigDecimal discount = calculateDiscount(selectedShippingVoucher.getVoucher(), shippingFee, productPrice, model);
            if (discount != null) {
                shippingDiscount = discount.min(shippingFee);
            }
        }

        orderDTO.setProductVoucherId(productVoucherId);
        orderDTO.setShippingVoucherId(shippingVoucherId);
        orderDTO.setDiscountAmount(productDiscount);
        orderDTO.setShippingFee(shippingFee.subtract(shippingDiscount));
        orderDTO.setFinalAmount(productPrice.add(orderDTO.getShippingFee()).subtract(productDiscount));

        model.put("selectedProductVoucher", selectedProductVoucher);
        model.put("selectedShippingVoucher", selectedShippingVoucher);
        model.put("selectedVoucher", selectedProductVoucher != null ? selectedProductVoucher : selectedShippingVoucher);
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal discountBase, BigDecimal minOrderBase,
            Map<String, Object> model) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }

        if (voucher.getMinOrderValue() != null && minOrderBase.compareTo(voucher.getMinOrderValue()) < 0) {
            model.put("voucherError", "Đơn hàng chưa đủ điều kiện để dùng voucher này");
            return null;
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("FIXED".equals(voucher.getDiscountType())) {
            discount = BigDecimal.valueOf(voucher.getDiscountValue());
        } else if ("PERCENT".equals(voucher.getDiscountType())) {
            discount = discountBase
                    .multiply(BigDecimal.valueOf(voucher.getDiscountValue()))
                    .divide(BigDecimal.valueOf(100));
        }

        if (discount.compareTo(discountBase) > 0) {
            discount = discountBase;
        }

        return discount;
    }

}