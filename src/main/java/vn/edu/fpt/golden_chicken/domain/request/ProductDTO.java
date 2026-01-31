package vn.edu.fpt.golden_chicken.domain.request;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO {
    Long id;
    String name;
    BigDecimal price;
    String description;
    boolean active;
    ProductType type;
    Long categoryId;
}
