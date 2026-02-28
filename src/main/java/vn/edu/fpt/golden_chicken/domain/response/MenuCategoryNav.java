package vn.edu.fpt.golden_chicken.domain.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuCategoryNav {
    String name;
    String img;     // URL dùng trực tiếp trong th:src
    String anchor;  // slug từ name
}