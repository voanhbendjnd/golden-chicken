package vn.edu.fpt.golden_chicken.domain.request;


import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherUpdateDTO {

    String code;
    String name;
    String description;

    Integer discountValue;
    String discountType;

    BigDecimal minOrderValue;

    Integer pointCost;

    LocalDateTime startAt;
    LocalDateTime endAt;
    boolean exchangeable;
    String status;

}
