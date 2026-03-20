package vn.edu.fpt.golden_chicken.domain.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResShippingFee {
    Long id;
    String ward;
    BigDecimal fee;
    LocalDateTime createdAt, updatedAt;
}
