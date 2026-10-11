package com.interacthub.controller;

import com.interacthub.dto.common.ApiResponse;
import com.interacthub.dto.notification.NotificationResponse;
import com.interacthub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController extends BaseController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "100") int take) {
        return ResponseEntity.ok(notificationService.getNotifications(getCurrentUserId(), take));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        int count = notificationService.markAllAsRead(getCurrentUserId());
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted", null));
    }
}
