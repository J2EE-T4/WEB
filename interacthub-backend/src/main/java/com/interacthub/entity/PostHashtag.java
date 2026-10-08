package com.interacthub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_hashtags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostHashtag {

    @EmbeddedId
    private PostHashtagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hashtagId")
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;
}
