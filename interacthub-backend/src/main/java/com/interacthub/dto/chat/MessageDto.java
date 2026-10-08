package com.interacthub.dto.chat;
import java.time.LocalDateTime;
public record MessageDto(String id, String senderId, String senderUsername, String senderAvatarUrl, String content, LocalDateTime createdAt, boolean isRead) {}
