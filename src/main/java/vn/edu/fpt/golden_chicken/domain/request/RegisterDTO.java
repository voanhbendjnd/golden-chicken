package vn.edu.fpt.golden_chicken.domain.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterDTO {
    String email;
    String name;
    String password;
    String phone;
}
