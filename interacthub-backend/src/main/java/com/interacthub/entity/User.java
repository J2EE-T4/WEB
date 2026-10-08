package com.interacthub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    private String address;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "ROLE_USER";

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    @OneToMany(mappedBy = "user") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "sender") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Friendship> sentFriendships = new ArrayList<>();

    @OneToMany(mappedBy = "receiver") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @OneToMany(mappedBy = "user") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Story> stories = new ArrayList<>();

    @OneToMany(mappedBy = "user") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "triggeredByUser") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<Notification> triggeredNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "reporter") @ToString.Exclude @JsonIgnore @Builder.Default
    private List<PostReport> postReports = new ArrayList<>();
}
