package com.interacthub.entity;

import com.interacthub.entity.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_medias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostMedia {

    @Id
    @Column(length = 36)
    @Builder.Default
    private String id = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 10)
    private MediaType mediaType;
}
