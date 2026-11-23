package com.interacthub.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO gửi qua WebSocket khi user đang gõ hoặc ngừng gõ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingSignalDto {
    private String conversationId;
    private String userId;
    private boolean typing;
}
