package com.interacthub.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.interacthub.security.CustomUserDetails;
import com.interacthub.service.PresenceService;

import lombok.RequiredArgsConstructor;

/**
 * Listener cho WebSocket connect/disconnect events.
 * Thay thế OnConnectedAsync / OnDisconnectedAsync trong NotificationHub.cs (.NET).
 *
 * Khi user connect → thêm connection vào Redis (PresenceService).
 * Khi user disconnect → xoá connection khỏi Redis.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = extractUserId(accessor);

        if (userId != null && sessionId != null) {
            presenceService.addConnection(userId, sessionId);
            log.info("WebSocket connected: user={}, session={}", userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = extractUserId(accessor);

        if (userId != null && sessionId != null) {
            presenceService.removeConnection(userId, sessionId);
            boolean stillOnline = presenceService.isUserOnline(userId);
            log.info("WebSocket disconnected: user={}, session={}, stillOnline={}", userId, sessionId, stillOnline);
        }
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
