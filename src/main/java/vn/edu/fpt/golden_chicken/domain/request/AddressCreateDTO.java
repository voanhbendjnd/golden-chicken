package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressCreateDTO {

    @NotBlank(message = "Recipient name cannot be empty")
    @Size(max = 255, message = "Recipient name must be <= 255 characters")
    String recipientName;

    @NotBlank(message = "Recipient phone cannot be empty")
    @Pattern(regexp = "\\d{10,15}", message = "Phone must be 10-15 digits")
    String recipientPhone;

    @NotBlank(message = "Specific address cannot be empty")
    @Size(max = 255, message = "Specific address must be <= 255 characters")
    String specificAddress;

    @NotBlank(message = "Ward cannot be empty")
    @Size(max = 255, message = "Ward must be <= 255 characters")
    String ward;

    @NotBlank(message = "District cannot be empty")
    @Size(max = 255, message = "District must be <= 255 characters")
    String district;

    Boolean isDefault;
}
