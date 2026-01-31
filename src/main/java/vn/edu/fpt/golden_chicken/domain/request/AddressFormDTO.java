package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressFormDTO {
    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name max 100")
    String recipientName;

    @NotBlank(message = "Specific address is required")
    @Size(max = 255, message = "Specific address max 255")
    String specificAddress;

    @NotBlank(message = "Ward is required")
    @Size(max = 100)
    private String ward;

    @NotBlank(message = "District is required")
    @Size(max = 100)
    private String district;

    private Boolean isDefault = false;
}
