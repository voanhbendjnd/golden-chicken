package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BadWordResponse {
    Long id;
    String word;
    Boolean status;
    Boolean applyFromNowOn;
}
