package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.annotations.Phone;

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
    String address;
    String password;
    String confirmPassword;
    Boolean status;
}
