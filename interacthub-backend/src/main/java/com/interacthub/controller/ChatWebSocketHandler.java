package com.interacthub.controller;

import com.interacthub.dto.chat.*;
import com.interacthub.security.CustomUserDetails;
import com.interacthub.service.ChatService;
import com.interacthub.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

/**
 * WebSocket STOMP message handler — thay thế NotificationHub.cs (.NET SignalR Hub).
 *
 * Client gửi message tới /app/chat.send, /app/chat.typing, /app/chat.read.
 * Server broadcast qua /topic/conversation/{id} hoặc /user/{userId}/queue/messages.
 *
 * Mapping từ .NET:
 *   Hub.SendChatMessage()          → @MessageMapping("/chat.send")
 *   Hub.StartTyping()              → @MessageMapping("/chat.typing")
 *   Hub.StopTyping()               → @MessageMapping("/chat.stopTyping")
 *   Hub.MarkConversationAsRead()   → @MessageMapping("/chat.read")
 *   Clients.Group(conversationId)  → messagingTemplate.convertAndSend("/topic/conversation/{id}", ...)
 *   Clients.User(userId)           → messagingTemplate.convertAndSendToUser(userId, "/queue/messages", ...)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler {

    private final ChatService chatService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client gửi tin nhắn chat.
     * Tương đương Hub method SendChatMessage trong NotificationHub.cs.
     */
    @MessageMapping("/chat.send")
    public void handleSendMessage(@Payload SendMessageRequest request,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String senderId = extractUserId(headerAccessor);
        if (senderId == null) return;

        if (request.content() == null || request.content().isBlank() || request.content().length() > 2000) {
            log.warn("User {} sent invalid message content", senderId);
            return;
        }

        try {
            MessageDto saved = chatService.sendMessage(senderId, request);
            if (saved == null) return;

            String convId = chatService.getConversationIdForUsers(senderId, request.receiverId());

            NewMessageSignalDto signal = NewMessageSignalDto.builder()
                    .messageId(saved.id())
                    .conversationId(convId)
                    .senderId(senderId)
                    .senderName(saved.senderUsername())
                    .senderAvatarUrl(saved.senderAvatarUrl())
                    .content(saved.content())
                    .sentAt(saved.createdAt())
                    .build();

            // Broadcast cho tất cả subscriber của conversation topic
            messagingTemplate.convertAndSend("/topic/conversation/" + convId, signal);

            // Push riêng cho receiver qua user queue (nếu chưa mở conversation)
            messagingTemplate.convertAndSendToUser(request.receiverId(), "/queue/messages", signal);

            log.debug("Chat message sent from {} in conversation {}", senderId, convId);

        } catch (Exception ex) {
            log.error("Error sending chat message from {}", senderId, ex);
        }
    }

    /**
     * Client báo đang gõ — tương đương Hub.StartTyping() trong .NET.
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingSignalDto payload,
                              SimpMessageHeaderAccessor headerAccessor) {
        String userId = extractUserId(headerAccessor);
        if (userId == null || payload.getConversationId() == null) return;

        payload.setUserId(userId);
        payload.setTyping(true);
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + payload.getConversationId() + "/typing", payload);
    }

    /**
     * Client báo ngừng gõ — tương đương Hub.StopTyping() trong .NET.
     */
    @MessageMapping("/chat.stopTyping")
    public void handleStopTyping(@Payload TypingSignalDto payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String userId = extractUserId(headerAccessor);
        if (userId == null || payload.getConversationId() == null) return;

        payload.setUserId(userId);
        payload.setTyping(false);
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + payload.getConversationId() + "/typing", payload);
    }

    /**
     * Client đánh dấu đã đọc — tương đương Hub.MarkConversationAsRead() trong .NET.
     */
    @MessageMapping("/chat.read")
    public void handleMarkAsRead(@Payload MessagesReadSignalDto payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String userId = extractUserId(headerAccessor);
        if (userId == null || payload.getConversationId() == null) return;

        chatService.markConversationAsRead(payload.getConversationId(), userId);
        payload.setReadByUserId(userId);
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + payload.getConversationId() + "/read", payload);
    }

    private String extractUserId(SimpMessageHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        }
        return null;
    }
}
