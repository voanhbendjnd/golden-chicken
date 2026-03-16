package vn.edu.fpt.golden_chicken.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BadWordDTO(Long id, @NotBlank String word, @NotNull Boolean status) {
}
