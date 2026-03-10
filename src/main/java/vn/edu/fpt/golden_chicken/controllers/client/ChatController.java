package vn.edu.fpt.golden_chicken.controllers.client;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import vn.edu.fpt.golden_chicken.domain.response.ChatMessage;

@Controller
public class ChatController {

    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    public ChatController(org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now().toString());

        // Đẩy tin nhắn lên Kafka, KafkaChatConsumer sẽ xử lý lưu Redis và gửi WebSocket
        this.kafkaTemplate.send("chat-topic", chatMessage);
    }
}