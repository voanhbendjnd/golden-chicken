package vn.edu.fpt.golden_chicken.domain.response;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResSingleProduct {
    Long id;
    String name;
    boolean active;
    String img;
    Integer quantity;
    BigDecimal price;
}
