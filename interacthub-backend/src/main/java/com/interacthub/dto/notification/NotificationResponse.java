package com.interacthub.dto.notification;
import com.interacthub.dto.post.UserResponse;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private String id, type, content, referenceId;
    private UserResponse triggeredBy;
    private boolean read;
    private LocalDateTime createdAt;
}
