package vn.edu.fpt.golden_chicken.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewMessage {
    String email;
    String comment;
    Integer record;
    String productName;
}
