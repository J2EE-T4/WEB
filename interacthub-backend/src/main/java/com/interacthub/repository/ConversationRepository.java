package com.interacthub.repository;

import com.interacthub.entity.Conversation;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    Optional<Conversation> findByUser1IdAndUser2Id(String user1Id, String user2Id);

    @Query("SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<Conversation> findAllByUserId(@Param("userId") String userId);
}
