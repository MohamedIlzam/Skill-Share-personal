package com.skillshare.skillshare.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePic;
    private String lastMessageContent;
    private Instant lastMessageTime;
    private Long unreadCount;
}
