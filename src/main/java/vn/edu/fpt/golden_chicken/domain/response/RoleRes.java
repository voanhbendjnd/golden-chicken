package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
public class RoleRes {
    Long id;
    String name;
    String description;
}
