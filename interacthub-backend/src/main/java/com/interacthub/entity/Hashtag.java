package com.interacthub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hashtags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hashtag {

    @Id
    @Column(length = 36)
    @Builder.Default
    private String id = java.util.UUID.randomUUID().toString();

    @Column(nullable = false, unique = true)
    private String content;
}
