package vn.edu.fpt.golden_chicken.domain.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShippingFeeDTO(
        Long id,
        @NotBlank(message = "Phường/xã không được bỏ trống") String ward,
        @NotNull(message = "Giá tiền không được bỏ trống") @Min(value = 0, message = "Giá phí không được bé hơn không") BigDecimal fee) {
    public ShippingFeeDTO() {
        this(null, "", BigDecimal.ZERO);
    }
}