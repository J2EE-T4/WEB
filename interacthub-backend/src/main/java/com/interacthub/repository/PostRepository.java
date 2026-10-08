package com.interacthub.repository;

import com.interacthub.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.*;

public interface PostRepository extends JpaRepository<Post, String> {

    @EntityGraph(attributePaths = {"user", "postMedias", "postHashtags", "postHashtags.hashtag", "likes", "comments", "reposts"})
    Optional<Post> findWithDetailsById(String id);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.postMedias WHERE p.createdAt < :cursor ORDER BY p.createdAt DESC")
    List<Post> findFeedPosts(@Param("cursor") LocalDateTime cursor, Pageable pageable);

    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserId(String userId);
}
