package vn.edu.fpt.golden_chicken.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "chat_messages")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(length = 255)
    String senderId;
    @Column(length = 255)
    String receiverId;
    @Column(columnDefinition = "NTEXT")
    String content;
    LocalDateTime timestamp;
    Boolean isRead = false;

    @PrePersist
    public void handleBeforeCreate() {
        this.timestamp = LocalDateTime.now();
    }
}