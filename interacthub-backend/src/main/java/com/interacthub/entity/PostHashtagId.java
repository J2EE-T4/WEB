package com.interacthub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PostHashtagId implements Serializable {

    @Column(name = "post_id", length = 36)
    private String postId;

    @Column(name = "hashtag_id", length = 36)
    private String hashtagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostHashtagId that)) return false;
        return Objects.equals(postId, that.postId) && Objects.equals(hashtagId, that.hashtagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, hashtagId);
    }
}
