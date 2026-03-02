package vn.edu.fpt.golden_chicken.domain.response;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.OrderStatus;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage implements Serializable {
    private Long orderId;
    private String customerEmail;
    private String customerName;
    private OrderStatus status;
    private BigDecimal totalPrice;

}
