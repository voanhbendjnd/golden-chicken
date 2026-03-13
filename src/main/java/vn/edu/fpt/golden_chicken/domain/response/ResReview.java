package vn.edu.fpt.golden_chicken.domain.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.utils.constants.ReviewStatus;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResReview {
    Long customerId;
    String name;
    Long id;
    List<String> mediaUrls;
    String comment;
    Integer rating;
    ReviewStatus reviewStatus;
    LocalDateTime createdAt, updatedAt;
    Boolean isUpdate;
}
