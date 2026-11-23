package com.interacthub.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO gửi qua WebSocket khi tin nhắn được đánh dấu đã đọc.
 * Tương đương MessagesReadSignalDto trong .NET.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagesReadSignalDto {
    private String conversationId;
    private String readByUserId;
}
