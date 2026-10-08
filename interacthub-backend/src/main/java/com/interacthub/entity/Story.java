package com.interacthub.entity;

import com.interacthub.entity.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "stories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Story {

    @Id
    @Column(length = 36)
    @Builder.Default
    private String id = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "media_url")
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 10)
    private MediaType mediaType;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
