package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BadWordDTO(Long id, @NotBlank(message = "Từ cấm không được bỏ trống") String word,
        @NotNull(message = "Trạng thái không đực bỏ trống") Boolean status) {
}
