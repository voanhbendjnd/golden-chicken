package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.annotations.Phone;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    Long id;
    @Email(message = "Email Incorrect Format!")
    @NotBlank(message = "Email Cannot Be Empty!")
    String email;
    @NotBlank(message = "Full Name Cannot Be Empty!")
    String fullName;
    @Phone
    String phone;
    String password;
    String confirmPassword;
    Boolean status;
    // @NotNull(message = "Role Cannot Be Empty!")
    Long roleId;
    StaffType staffType;
    String address;
    String district;
    String ward;
}
