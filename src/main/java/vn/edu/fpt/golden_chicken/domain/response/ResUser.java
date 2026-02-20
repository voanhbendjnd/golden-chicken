package vn.edu.fpt.golden_chicken.domain.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResUser {
    Long id;
    String fullName;
    String email;
    String phone;
    Boolean status;
    String avatar;
    Long roleId;
    StaffType staffType;
    String createdBy, updatedBy;
    LocalDateTime createdAt, updatedAt;

}
