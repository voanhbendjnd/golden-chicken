package vn.edu.fpt.golden_chicken.domain.response;

import java.io.Serializable;
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
// dich sang json can serial
public class ActionPointMessage implements Serializable {
    Long userId;
    Long change;
    String action;
    String reason;
    LocalDateTime actionAt;
}
