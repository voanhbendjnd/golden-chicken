package vn.edu.fpt.golden_chicken.domain.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PACKAGE)
public class ResRole {
    Long id;
    String name;
    String description;
    List<Permission> permissions;

    @Data
    @AllArgsConstructor
    public static class Permission {
        private Long id;
        private String name;
    }
}
