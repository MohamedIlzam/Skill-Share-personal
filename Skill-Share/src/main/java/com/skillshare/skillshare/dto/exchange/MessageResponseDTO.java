package com.skillshare.skillshare.dto.exchange;

import java.time.Instant;

public class MessageResponseDTO {
    private Long id;
    private Long exchangeRequestId;
    private Long senderId;
    private String senderName;
    private String senderProfilePic;
    private String content;
    private Instant createdAt;
    private boolean isRead;

    public MessageResponseDTO(Long id, Long exchangeRequestId, Long senderId, String senderName, String senderProfilePic, String content, Instant createdAt, boolean isRead) {
        this.id = id;
        this.exchangeRequestId = exchangeRequestId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePic = senderProfilePic;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public Long getId() {
        return id;
    }

    public Long getExchangeRequestId() {
        return exchangeRequestId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderProfilePic() {
        return senderProfilePic;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }
}
