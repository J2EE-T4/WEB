package com.interacthub.repository;

import com.interacthub.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.*;

public interface StoryRepository extends JpaRepository<Story, String> {
    List<Story> findByExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime now);
    List<Story> findByUserIdInAndExpiresAtAfterOrderByCreatedAtDesc(List<String> userIds, LocalDateTime now);
    void deleteByExpiresAtBefore(LocalDateTime now);
    Optional<Story> findByIdAndUserId(String id, String userId);
}
