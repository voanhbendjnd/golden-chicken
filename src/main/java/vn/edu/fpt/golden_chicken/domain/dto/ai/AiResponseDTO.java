package vn.edu.fpt.golden_chicken.domain.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiResponseDTO {
    private String action; // "CHAT" or "ADD_TO_CART"
    private String message;
    private List<ItemDTO> items;
}
