package vn.edu.fpt.golden_chicken.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.edu.fpt.golden_chicken.domain.entity.ChatMessage;
import vn.edu.fpt.golden_chicken.domain.response.ChatMessageDTO;
import vn.edu.fpt.golden_chicken.domain.response.ChatPartnerPreview;
import vn.edu.fpt.golden_chicken.repositories.ChatMessageRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private static final int PAGE_SIZE = 20;

    private final ConcurrentHashMap<String, LocalDateTime> seenCache = new ConcurrentHashMap<>();

    @Transactional
    public void saveMessage(ChatMessageDTO dto) {
        ChatMessage entity = new ChatMessage();
        entity.setSenderId(dto.getSenderId());
        entity.setReceiverId(dto.getRecipientId());
        entity.setContent(dto.getContent());

        String cacheKey = dto.getSenderId() + ":" + dto.getRecipientId();
        LocalDateTime seenAt = seenCache.get(cacheKey);
        boolean isRead = false;

        if (seenAt != null && dto.getTimestamp() != null) {
            try {
                String rawTs = dto.getTimestamp()
                        .replace("Z", "")
                        .replaceAll("\\[.*\\]", "")
                        .trim();
                LocalDateTime msgTime = LocalDateTime.parse(rawTs);
                isRead = !msgTime.isAfter(seenAt);
            } catch (Exception ignored) {
            }
        }

        entity.setIsRead(isRead);
        chatMessageRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getHistory(String user1, String user2, int page) {
        var pageable = PageRequest.of(page, PAGE_SIZE);
        var entities = chatMessageRepository.findConversation(user1, user2, pageable);
        List<ChatMessageDTO> list = entities.stream().map(this::toDto).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    @Transactional
    public void markSeen(String sender, String recipient) {
        chatMessageRepository.markAsRead(sender, recipient);

        String cacheKey = sender + ":" + recipient;
        seenCache.put(cacheKey, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<String> getChatPartners(String user) {
        if ("STAFF".equalsIgnoreCase(user)) {
            return chatMessageRepository.findAllChatPartnersOfStaff();
        }
        return List.of("STAFF");
    }

    @Transactional(readOnly = true)
    public List<ChatPartnerPreview> getPartnersWithPreview() {
        List<String> partners = chatMessageRepository.findAllChatPartnersOfStaff();
        List<ChatPartnerPreview> result = new ArrayList<>();

        for (String partner : partners) {
            ChatPartnerPreview preview = new ChatPartnerPreview();
            preview.setPartnerId(partner);
            var lastMsgs = chatMessageRepository.findLatestMessage(partner, "STAFF", PageRequest.of(0, 1));
            if (!lastMsgs.isEmpty()) {
                var last = lastMsgs.get(0);
                preview.setLastMessage(last.getContent());
                preview.setLastTimestamp(last.getTimestamp() != null
                        ? last.getTimestamp().toString()
                        : null);
                preview.setLastSenderIsStaff("STAFF".equals(last.getSenderId()));
            }
            long unread = chatMessageRepository.countUnread(partner, "STAFF");
            preview.setUnreadCount((int) unread);
            result.add(preview);
        }

        result.sort((a, b) -> {
            if (a.getUnreadCount() != b.getUnreadCount())
                return b.getUnreadCount() - a.getUnreadCount();
            String ta = a.getLastTimestamp(), tb = b.getLastTimestamp();
            if (ta == null && tb == null)
                return 0;
            if (ta == null)
                return 1;
            if (tb == null)
                return -1;
            return tb.compareTo(ta);
        });

        return result;
    }

    @Transactional(readOnly = true)
    public List<String> searchPartners(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return chatMessageRepository.findAllChatPartnersOfStaff();
        }
        return chatMessageRepository.searchPartnersByKeyword(keyword.trim());
    }

    private ChatMessageDTO toDto(ChatMessage e) {
        var dto = new ChatMessageDTO();
        dto.setId(e.getId());
        dto.setSenderId(e.getSenderId());
        dto.setRecipientId(e.getReceiverId());
        dto.setContent(e.getContent());
        dto.setTimestamp(e.getTimestamp() != null ? e.getTimestamp().toString() : null);
        dto.setSeen(Boolean.TRUE.equals(e.getIsRead()));
        return dto;
    }
}
