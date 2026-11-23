package com.interacthub.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO gửi qua WebSocket khi có tin nhắn mới.
 * Tương đương NewMessageSignalDto trong .NET.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMessageSignalDto {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private LocalDateTime sentAt;
}
