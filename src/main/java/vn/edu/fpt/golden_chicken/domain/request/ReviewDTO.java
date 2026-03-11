package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewDTO {
    Long id;
    String comment;
    @Min(value = 1, message = "Min rating is 1")
    @Max(value = 5, message = "Max rating is 5")
    Integer rating;
    // Long orderItemId;
}
