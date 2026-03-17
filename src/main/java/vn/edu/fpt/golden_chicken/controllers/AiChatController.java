package vn.edu.fpt.golden_chicken.controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.golden_chicken.services.AiChatService;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody AiChatRequest request) {
        Map<String, Object> response = aiChatService.processChat(request.getChatMessage(), request.getCustomerId());
        return ResponseEntity.ok(response);
    }

    @Data
    public static class AiChatRequest {
        private String chatMessage;
        private Long customerId;
    }
}
