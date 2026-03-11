package vn.edu.fpt.golden_chicken.domain.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResReview {
    String name;
    Long id;
    List<String> mediaUrls;
    String comment;
    Integer rating;
    LocalDateTime createdAt, updatedAt;
}
