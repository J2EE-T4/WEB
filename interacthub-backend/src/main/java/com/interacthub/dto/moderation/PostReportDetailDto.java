package com.interacthub.dto.moderation;
import com.interacthub.dto.post.UserResponse;
import java.time.LocalDateTime;
public record PostReportDetailDto(String reportId, UserResponse reporter, String reason, LocalDateTime createdAt) {}
