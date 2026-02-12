package vn.edu.fpt.golden_chicken.domain.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    BigDecimal totalPrice;
    int totalQuantity;
    List<CartItemDTO> items = new ArrayList<>();

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CartItemDTO {
        Long itemId;
        Long productId;
        String productName;
        String productImg;
        int quantity;
        BigDecimal price;
        BigDecimal subTotal;

    }

}
