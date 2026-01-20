package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.annotations.Phone;
import vn.edu.fpt.golden_chicken.utils.annotations.StrongPassword;
import vn.edu.fpt.golden_chicken.utils.constants.StaffType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
// @StrongPassword
public class UserDTO {
    Long id;
    @Email(message = "Email Incorrect Format!")
    @NotBlank(message = "Email Cannot Be Empty!")
    String email;
    @NotBlank(message = "Full Name Cannot Be Empty!")
    String fullName;
    @Phone
    String phone;
    @Size(min = 6, message = "Password Must Be At Least 6 Characters")
    String password;
    @Size(min = 6, message = "Confirm Password Must Be At Least 6 Characters")
    String confirmPassword;
    Boolean status;
    // @NotNull(message = "Role Cannot Be Empty!")
    Long roleId;
    StaffType staffType;
    String address;
    String district;
    String ward;
}
