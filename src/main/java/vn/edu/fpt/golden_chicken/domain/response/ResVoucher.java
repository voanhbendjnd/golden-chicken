package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResVoucher {

    Long id;
    String code;
    String name;
    String description;

    Integer discountValue;
    String discountType;
    BigDecimal minOrderValue;

    Integer pointCost;
    Boolean exchangeable;

    String status;

    LocalDateTime startAt;
    LocalDateTime endAt;
}