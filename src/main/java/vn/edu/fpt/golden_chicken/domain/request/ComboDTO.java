package vn.edu.fpt.golden_chicken.domain.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboDTO {
    // List<Long> productIds;
    Long id;
    String name;
    BigDecimal price;
    String description;
    boolean active;
    ProductType type = ProductType.COMBO;
    String img;
    List<String> imgs;
    Category category;
    List<ProductItem> items;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductItem {
        Long id;
        Integer quantity;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Category {
        Long id;
        String name;
    }
}
