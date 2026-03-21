package vn.edu.fpt.golden_chicken.domain.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class BadWordDTO {
        private Long id;
        @NotBlank(message = "Từ cấm không được bỏ trống")
        private String word;
        @NotNull(message = "Trạng thái không được bỏ trống")
        private Boolean status;
        private Boolean applyFromNowOn;

}