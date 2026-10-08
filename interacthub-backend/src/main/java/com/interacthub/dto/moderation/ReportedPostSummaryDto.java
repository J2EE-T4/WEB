package com.interacthub.dto.moderation;
import com.interacthub.dto.post.UserResponse;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportedPostSummaryDto {
    private String postId, postContent;
    private UserResponse postAuthor;
    private long reportCount;
    private LocalDateTime lastReportedAt;
}
