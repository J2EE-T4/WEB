package com.interacthub.repository;

import com.interacthub.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

    List<Message> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(String conversationId, LocalDateTime before, Pageable pageable);

    List<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :convId AND m.sender.id <> :userId AND m.isRead = false")
    long countUnread(@Param("convId") String convId, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :convId AND m.sender.id <> :userId AND m.isRead = false")
    int markAllAsRead(@Param("convId") String convId, @Param("userId") String userId);
}
