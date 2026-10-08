package com.interacthub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FriendshipId implements Serializable {

    @Column(name = "sender_id", length = 36)
    private String senderId;

    @Column(name = "receiver_id", length = 36)
    private String receiverId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipId that)) return false;
        return Objects.equals(senderId, that.senderId) && Objects.equals(receiverId, that.receiverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId);
    }
}
