package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRes {
    Long id;
    String fullName;
    String email;
    String phone;
    Boolean status;
    Long roleId;
    StaffType staffType;
}
