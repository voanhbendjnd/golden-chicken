package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRes {
    Long id;
    String fullName;
    String email;
    String phone;
    String address;
    Boolean status;
    Long currentPoints;
}
