package com.interacthub.service;

import com.interacthub.dto.notification.NotificationResponse;
import com.interacthub.dto.post.UserResponse;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.NotificationType;
import com.interacthub.exception.ResourceNotFoundException;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/**
 * Service quản lý thông báo — lưu DB + push realtime qua WebSocket.
 * Tương đương NotificationService.cs trong .NET (dùng IHubContext<NotificationHub>).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo notification, lưu DB và push realtime qua WebSocket.
     * Tương đương CreateAndSendNotificationAsync() trong .NET.
     */
    @Transactional
    public void createAndPushNotification(String userId, String triggeredByUserId,
                                           NotificationType type, String referenceId, String content) {
        if (userId.equals(triggeredByUserId)) return;
        User user = userRepository.findById(userId).orElse(null);
        User triggeredBy = userRepository.findById(triggeredByUserId).orElse(null);
        if (user == null || triggeredBy == null) return;

        Notification n = Notification.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .triggeredByUser(triggeredBy)
                .type(type)
                .referenceId(referenceId)
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        notificationRepository.save(n);

        // Push realtime via WebSocket — thay thế SignalR HubContext
        try {
            NotificationResponse response = toResponse(n);
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", response);
            log.debug("Notification pushed to user {} via WebSocket", userId);
        } catch (Exception ex) {
            log.error("Error pushing notification to user {}", userId, ex);
        }
    }

    /**
     * Backward-compatible method — gọi createAndPushNotification.
     */
    @Transactional
    public void createNotification(String userId, String triggeredByUserId,
                                    NotificationType type, String referenceId, String content) {
        createAndPushNotification(userId, triggeredByUserId, type, referenceId, content);
    }

    public List<NotificationResponse> getNotifications(String userId, int take) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, take))
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public boolean markAsRead(String notificationId, String userId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        n.setRead(true);
        notificationRepository.save(n);
        return true;
    }

    @Transactional
    public int markAllAsRead(String userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notificationRepository.delete(n);
    }

    private NotificationResponse toResponse(Notification n) {
        UserResponse tb = null;
        if (n.getTriggeredByUser() != null) {
            User u = n.getTriggeredByUser();
            tb = new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
        }
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType() != null ? n.getType().name() : null)
                .content(n.getContent())
                .referenceId(n.getReferenceId())
                .triggeredBy(tb)
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
