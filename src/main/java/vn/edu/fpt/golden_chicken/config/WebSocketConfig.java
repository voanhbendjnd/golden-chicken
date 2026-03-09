package vn.edu.fpt.golden_chicken.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Các tin nhắn từ Server gửi xuống Client sẽ bắt đầu bằng /topic hoặc /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Các tin nhắn từ Client gửi lên Server sẽ bắt đầu bằng /app
        config.setApplicationDestinationPrefixes("/app");
        // Gửi tin nhắn riêng cho từng User
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cổng kết nối WebSocket (Handshake)
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*").withSockJS();
    }
}