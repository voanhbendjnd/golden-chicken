package vn.edu.fpt.golden_chicken.services.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessageDTO;
import vn.edu.fpt.golden_chicken.services.ChatService;

@Service
@RequiredArgsConstructor
public class KafkaChatConsumer {

    private final ChatService chatService;

    @KafkaListener(topics = "chat-topic", groupId = "chat-db-persist-group")
    public void consumeChat(ChatMessageDTO message) {
        chatService.saveMessage(message);
    }
}
