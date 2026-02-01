package vn.edu.fpt.golden_chicken.domain.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    @NotBlank(message = "Name Cannot be Empty!")
    String name;
    @NotNull(message = "Price Cannot be Empty!")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0!")
    @DecimalMax(value = "999999999.99", message = "Price is too large!")
    BigDecimal price;
    @NotBlank(message = "Description Cannot be Empty!")
    String description;
    @NotNull
    boolean active;
    @NotNull(message = "Product Type Cannot be Empty!")
    ProductType type;
    Category category = new Category();
    String img;
    List<String> imgs;

    @Data
    public static class Category {
        Long id;
        String name;
    }
}
