package com.interacthub.dto.profile;
import lombok.*;
import java.time.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileResponse {
    private String id, username, displayName, email, avatarUrl, bio;
    private LocalDate dateOfBirth;
    private String gender, address;
    private LocalDateTime createdAt;
    private long postCount, followerCount, followingCount;
    private boolean following;
}
