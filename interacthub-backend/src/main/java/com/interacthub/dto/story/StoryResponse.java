package com.interacthub.dto.story;
import com.interacthub.dto.post.UserResponse;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StoryResponse {
    private String id;
    private UserResponse author;
    private String mediaUrl, mediaType, caption;
    private LocalDateTime createdAt, expiresAt;
}
