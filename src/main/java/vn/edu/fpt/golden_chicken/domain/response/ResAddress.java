package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResAddress {
    Long id;
    String recipientName;
    String recipientPhone;
    String specificAddress;
    String ward;
    String city;
    Boolean isDefault;
}
