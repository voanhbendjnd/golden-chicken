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
    private final ShippingFeeService shippingFeeService;

    public CheckoutResponse buildCheckout(
            Long productId,
            List<Long> ids,
            Long orderId,
            Long productVoucherId,
            Long shippingVoucherId,
            Long addressId,
            Integer quantity) throws PermissionException {

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
            detail.setQuantity(quantity != null ? quantity : 1);

            details.add(detail);

            totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity != null ? quantity : 1));

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

            // if (cartItems.isEmpty()) {
            // response.setRedirect("redirect:/cart");
            // return response;
            // }

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
        } else {
            response.setRedirect("redirect:/address/config");
            return response;
        }
        if (selectedAddress.getWard() == null) {

        }
        BigDecimal shippingFee = this.shippingFeeService.getFeeByWard(selectedAddress.getWard());
        if (shippingFee == null) {
            response.setRedirect("redirect:/home");
            return response;
        }
        orderDTO.setItems(details);
        orderDTO.setTotalProductPrice(totalPrice);
        orderDTO.setShippingFee(shippingFee);
        orderDTO.setDiscountAmount(BigDecimal.ZERO);
        orderDTO.setFinalAmount(totalPrice.add(shippingFee));
        orderDTO.setPaymentMethod(PaymentMethod.COD);

        var currentUser = profileService.getCurrentUser();

        List<CustomerVoucher> vouchers = voucherService.getCustomerVouchers(currentUser.getId());

        model.put("vouchers", vouchers);

        CustomerVoucher productVoucher = voucherService.getCustomerVoucherById(productVoucherId);
        CustomerVoucher shippingVoucher = voucherService.getCustomerVoucherById(shippingVoucherId);
        String errorMessage = applyVouchersToOrder(productVoucher, shippingVoucher, orderDTO, shippingFee);

        model.put("order", orderDTO);
        model.put("defaultAddress", selectedAddress);
        model.put("selectedProductVoucher", productVoucher);
        model.put("selectedShippingVoucher", shippingVoucher);
        model.put("orderId", orderId);
        if (errorMessage != null) {
            model.put("voucherError", errorMessage);
        }

        return response;
    }

    private String applyVouchersToOrder(CustomerVoucher productVoucher,
            CustomerVoucher shippingVoucher,
            OrderDTO orderDTO,
            BigDecimal shippingFee) {
        BigDecimal productPrice = orderDTO.getTotalProductPrice();
        BigDecimal productDiscount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        String errorMessage = null;

        if (productVoucher != null) {
            Voucher voucher = productVoucher.getVoucher();
            try {
                productDiscount = calculateDiscount(voucher, productPrice, productPrice);
            } catch (IllegalArgumentException ex) {
                errorMessage = ex.getMessage();
            }
        }

        if (shippingVoucher != null) {
            Voucher voucher = shippingVoucher.getVoucher();
            try {
                shippingDiscount = calculateDiscount(voucher, shippingFee, productPrice).min(shippingFee);
            } catch (IllegalArgumentException ex) {
                errorMessage = ex.getMessage();
            }
        }

        if (productDiscount == null) {
            productDiscount = BigDecimal.ZERO;
        }
        if (shippingDiscount == null) {
            shippingDiscount = BigDecimal.ZERO;
        }

        orderDTO.setShippingFee(shippingFee);
        orderDTO.setProductDiscountAmount(productDiscount);
        orderDTO.setShippingDiscountAmount(shippingDiscount);
        orderDTO.setDiscountAmount(productDiscount.add(shippingDiscount));
        orderDTO.setFinalAmount(productPrice.add(shippingFee).subtract(orderDTO.getDiscountAmount()));

        orderDTO.setProductVoucherId(productVoucher != null ? productVoucher.getId() : null);
        orderDTO.setShippingVoucherId(shippingVoucher != null ? shippingVoucher.getId() : null);

        return errorMessage;
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal discountBase, BigDecimal minOrderBase) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }

        if (voucher.getMinOrderValue() != null && minOrderBase.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đủ điều kiện để dùng voucher này");
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

    public BigDecimal calculateOrderTotal(Long productId, List<Long> productIds, Integer quantity, Long orderId)
            throws PermissionException {
        if (orderId != null) {
            ResOrder order = orderService.findById(orderId);
            return order != null ? order.getFinalAmount() : BigDecimal.ZERO;
        }
        if (productId != null) {
            var product = productService.findById(productId);
            return product != null ? product.getPrice().multiply(BigDecimal.valueOf(quantity != null ? quantity : 1))
                    : BigDecimal.ZERO;
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

}
