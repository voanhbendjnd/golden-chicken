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
    @NotBlank(message = "Tên không được để trống")
    String name;
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải lớn hơn 0")
    @DecimalMax(value = "999999999.99", message = "Giá đạt cực đại")
    BigDecimal price;
    String description;
    @NotNull
    boolean active;
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
