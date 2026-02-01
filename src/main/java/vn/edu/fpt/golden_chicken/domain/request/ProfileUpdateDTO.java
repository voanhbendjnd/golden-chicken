package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateDTO {

    Long id;

    @NotBlank(message = "Full Name Cannot Be Empty!")
    @Size(max = 255, message = "Full Name Must Be <= 255 Characters")
    String fullName;

    // allow empty, otherwise 10-15 digits
    @Pattern(regexp = "(^$|\\d{10,15}$)", message = "Phone must be 10-15 digits")
    String phone;
}
