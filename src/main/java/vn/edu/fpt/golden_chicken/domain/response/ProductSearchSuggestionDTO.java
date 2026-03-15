package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * DTO nhẹ cho API gợi ý tìm kiếm sản phẩm (header, hero search).
 * Chỉ trả id, name để tối ưu hiệu năng.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchSuggestionDTO {
    Long id;
    String name;

    public ProductSearchSuggestionDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
