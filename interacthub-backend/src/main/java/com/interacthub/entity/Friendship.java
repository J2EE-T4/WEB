package com.interacthub.entity;

import com.interacthub.entity.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "friendships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("senderId")
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("receiverId")
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);
}
