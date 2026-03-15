package vn.edu.fpt.golden_chicken.domain.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordRegister {
    String email;
    String password;
    String confirmPassword;
    String otp;
}
