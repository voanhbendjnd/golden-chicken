package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResPermission {
    Long id;
    String name;
    String apiPath;
    String method;
    String module;
}
