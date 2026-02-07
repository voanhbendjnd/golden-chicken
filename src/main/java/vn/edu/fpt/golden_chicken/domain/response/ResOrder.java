package vn.edu.fpt.golden_chicken.domain.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResOrder {
    Long id;

    String name;
    String address;
    String phone;
    String note;

    BigDecimal totalPrice;
    String paymentMethod;
    String paymentStatus;

    OrderStatus status;
    LocalDateTime createdAt;

    List<OrderDetail> items;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderDetail {
        Long id;
        String name;
        Long productId;
        Integer quantity;
        BigDecimal price;
        String img;
    }

    Long customerId;

}
