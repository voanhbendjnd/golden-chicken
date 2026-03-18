package vn.edu.fpt.golden_chicken.controllers.client;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessageDTO;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now().toString());
        chatMessage.setSeen(false);
        if ("STAFF".equals(chatMessage.getRecipientId())) {
            messagingTemplate.convertAndSend("/topic/staffChat", chatMessage);
        } else {
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipientId(), "/queue/messages", chatMessage);
        }
        kafkaTemplate.send("chat-topic", chatMessage);
    }

    @MessageMapping("/chat.seen")
    public void notifySeen(@Payload Map<String, String> payload) {
        String from = payload.get("from");
        String to = payload.get("to");
        var seenEvent = Map.of("type", "SEEN", "by", from);
        if ("STAFF".equals(to)) {
            messagingTemplate.convertAndSend("/topic/staffSeenEvent", seenEvent);
        } else {
            messagingTemplate.convertAndSendToUser(to, "/queue/seen", seenEvent);
        }
    }
}