package vn.edu.fpt.golden_chicken.controllers.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessageDTO;
import vn.edu.fpt.golden_chicken.domain.response.ChatPartnerPreview;
import vn.edu.fpt.golden_chicken.services.ChatService;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getHistory(
            @RequestParam("user1") String user1,
            @RequestParam("user2") String user2,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "viewer", required = false) String viewer) {

        List<ChatMessageDTO> messages = chatService.getHistory(user1, user2, page);
        if (page == 0 && viewer != null && !viewer.isBlank()) {
            String sender = viewer.equals(user2) ? user1 : user2;
            chatService.markSeen(sender, viewer);
        }

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/seen")
    public ResponseEntity<Void> markSeen(
            @RequestParam("sender") String sender,
            @RequestParam("recipient") String recipient) {
        chatService.markSeen(sender, recipient);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/partners")
    public ResponseEntity<List<String>> getPartners(@RequestParam("user") String user) {
        return ResponseEntity.ok(chatService.getChatPartners(user));
    }

    /**
     * Rich partner list for staff: includes last message preview, timestamp, unread
     * count.
     * Sorted: unread first, then newest message first.
     */
    @GetMapping("/partners/preview")
    public ResponseEntity<List<ChatPartnerPreview>> getPartnersPreview() {
        return ResponseEntity.ok(chatService.getPartnersWithPreview());
    }

    /**
     * Search partners by customer name/email or message content.
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> searchPartners(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        return ResponseEntity.ok(chatService.searchPartners(keyword));
    }
}