package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherCreateDTO {

    @NotBlank(message = "Code is required")
    String code;

    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Description is required")
    String description;

    @NotNull(message = "Discount value is required")
    Integer discountValue;

    @NotBlank(message = "Discount type is required")
    String discountType;

    @NotNull(message = "Min order value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Min order value must be >= 0")
    BigDecimal minOrderValue;

    @NotNull(message = "Point cost is required")
    @Min(value = 0, message = "Point cost must be >= 0")
    Integer pointCost;

    @NotNull(message = "Exchangeable is required")
    Boolean exchangeable;

    String status;

    @NotNull(message = "Start time is required")
    LocalDateTime startAt;

    @NotNull(message = "End time is required")
    LocalDateTime endAt;

    @NotNull(message = "Quantity is required")
    Integer quantity ;

    @NotBlank(message = "Voucher Type is required")
    String voucherType ;
}
