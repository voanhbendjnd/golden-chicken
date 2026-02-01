package vn.edu.fpt.golden_chicken.domain.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ProductType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO {
    Long id;
    @NotBlank
    String name;
    @NotNull
    BigDecimal price;
    @NotBlank
    String description;
    @NotBlank
    boolean active;
    @NotBlank
    ProductType type;
    @NotBlank
    Long categoryId;
}
