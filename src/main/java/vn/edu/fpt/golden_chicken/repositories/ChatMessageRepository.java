package vn.edu.fpt.golden_chicken.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.fpt.golden_chicken.domain.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE (m.senderId = :user1 AND m.receiverId = :user2)
               OR (m.senderId = :user2 AND m.receiverId = :user1)
            ORDER BY m.timestamp DESC
            """)
    Page<ChatMessage> findConversation(
            @Param("user1") String user1,
            @Param("user2") String user2,
            Pageable pageable);

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE (m.senderId = :user1 AND m.receiverId = :user2)
               OR (m.senderId = :user2 AND m.receiverId = :user1)
            ORDER BY m.timestamp DESC
            """)
    List<ChatMessage> findLatestMessage(
            @Param("user1") String user1,
            @Param("user2") String user2,
            Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            WHERE m.senderId = :sender AND m.receiverId = :recipient AND m.isRead = false
            """)
    long countUnread(@Param("sender") String sender, @Param("recipient") String recipient);

    @Modifying
    @Query("""
            UPDATE ChatMessage m SET m.isRead = true WHERE m.senderId = :sender AND m.receiverId = :recipient AND m.isRead = false""")
    int markAsRead(@Param("sender") String sender, @Param("recipient") String recipient);

    @Query("""
            SELECT DISTINCT
                CASE WHEN m.senderId = 'STAFF' THEN m.receiverId ELSE m.senderId END
            FROM ChatMessage m
            WHERE m.senderId = 'STAFF' OR m.receiverId = 'STAFF'
            """)
    List<String> findAllChatPartnersOfStaff();

    @Query("""
            SELECT DISTINCT
                CASE WHEN m.senderId = 'STAFF' THEN m.receiverId ELSE m.senderId END
            FROM ChatMessage m
            WHERE (m.senderId = 'STAFF' OR m.receiverId = 'STAFF')
              AND (LOWER(m.senderId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.receiverId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.content)   LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<String> searchPartnersByKeyword(@Param("keyword") String keyword);
}
