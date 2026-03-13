package vn.edu.fpt.golden_chicken.domain.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserMessage {
    String email;
    String name;
    String password;
    String reason;
    LocalDateTime sendAt;
}
