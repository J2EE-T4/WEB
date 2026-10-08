package com.interacthub.dto.chat;
import com.interacthub.dto.post.UserResponse;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConversationDto {
    private String id;
    private UserResponse otherUser;
    private MessageDto lastMessage;
    private long unreadCount;
}
