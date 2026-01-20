package vn.edu.fpt.golden_chicken.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleDTO {
    Long id;
    @NotBlank(message = "Role Name Cannot Be Empty!")
    String name;
    String description;
    List<Long> permissionIds;
}
