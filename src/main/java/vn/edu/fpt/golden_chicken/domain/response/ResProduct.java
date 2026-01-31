package vn.edu.fpt.golden_chicken.domain.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResProduct {
    Long id;
    String name;
    BigDecimal price;
    String description;
    boolean active;
    ProductType type;
    String categoryName;
    String img;
    List<String> imgs;

}
