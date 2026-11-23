package com.interacthub.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request gửi tin nhắn qua WebSocket.
 * Tương đương SendMessageRequest trong .NET.
 */
public record SendMessageRequest(
        @NotBlank String receiverId,
        @NotBlank @Size(max = 2000) String content
) {}
