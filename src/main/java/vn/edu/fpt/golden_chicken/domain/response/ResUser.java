package vn.edu.fpt.golden_chicken.domain.response;

import java.time.Instant;
import java.util.Date;

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
    Long roleId;
    StaffType staffType;
    String createdBy, updatedBy;
    Date createdAt, updatedAt;

}
