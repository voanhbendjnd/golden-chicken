package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionDTO {
    Long id;
    @NotBlank(message = "Name Cannot Be Empty!")
    String name;
    @NotBlank(message = "API Path Cannot Be Empty!")
    String apiPath;
    @NotBlank(message = "Method Cannot Be Empty!")
    String method;
    @NotBlank(message = "Module Cannot Be Empty!")
    String module;
}
