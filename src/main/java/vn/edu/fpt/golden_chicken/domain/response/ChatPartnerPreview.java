package vn.edu.fpt.golden_chicken.domain.response;

import lombok.Data;

@Data
public class ChatPartnerPreview {
    private String partnerId;
    private String lastMessage;
    private String lastTimestamp;
    private boolean lastSenderIsStaff;
    private int unreadCount;
}
