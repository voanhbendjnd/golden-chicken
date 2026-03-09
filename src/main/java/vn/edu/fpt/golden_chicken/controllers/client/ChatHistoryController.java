package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessage;
import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final RedisUserService redisUserService;

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestParam("user1") String user1,
            @RequestParam("user2") String user2) {
        return ResponseEntity.ok(this.redisUserService.getChatHistory(user1, user2));
    }

    @GetMapping("/partners")
    public ResponseEntity<Set<String>> getPartners(@RequestParam("user") String user) {
        return ResponseEntity.ok(this.redisUserService.getChatPartners(user));
    }
}