package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class RoleRequest {
    Long id;
    @NotBlank(message = "Role Name Cannot Be Empty!")
    String name;
    String description;
}
