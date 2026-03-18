package vn.edu.fpt.golden_chicken.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long id;
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
    private boolean seen;
}