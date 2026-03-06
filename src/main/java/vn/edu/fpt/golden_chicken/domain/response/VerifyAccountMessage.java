package vn.edu.fpt.golden_chicken.domain.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyAccountMessage {
    String description;
    String email;
    // String otp;
    LocalDateTime createdAt;
}
