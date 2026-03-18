package vn.edu.fpt.golden_chicken.services.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessage;
import vn.edu.fpt.golden_chicken.services.redis.RedisUserService;

@Service
@RequiredArgsConstructor
public class KafkaChatConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisUserService redisUserService;

    @KafkaListener(topics = "chat-topic", groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void consumeChat(ChatMessage message) {
        redisUserService.saveChatMessageToRedis(message);
        if ("STAFF".equals(message.getRecipientId())) {
            messagingTemplate.convertAndSend("/topic/staffChat", message);
        } else {
            messagingTemplate.convertAndSendToUser(
                    message.getRecipientId(), "/queue/messages", message);
        }
    }
}
