package vn.edu.fpt.golden_chicken.domain.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.PaymentMethod;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    String name;
    String address;
    String phone;
    String note;
    BigDecimal totalProductPrice;
    BigDecimal shippingFee;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    PaymentMethod paymentMethod;

    List<OrderDetail> items;

    @Data
    public static class OrderDetail {
        Long productId;
        Integer quantity;
    }
}
