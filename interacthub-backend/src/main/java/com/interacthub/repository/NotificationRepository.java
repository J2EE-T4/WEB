package com.interacthub.repository;

import com.interacthub.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId);
}
