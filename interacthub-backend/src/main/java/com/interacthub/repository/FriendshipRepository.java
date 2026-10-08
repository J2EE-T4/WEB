package com.interacthub.repository;

import com.interacthub.entity.Friendship;
import com.interacthub.entity.FriendshipId;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    @Query("SELECT f FROM Friendship f JOIN FETCH f.sender JOIN FETCH f.receiver WHERE (f.id.senderId = :userId OR f.id.receiverId = :userId) AND f.status = com.interacthub.entity.enums.FriendshipStatus.ACCEPTED")
    List<Friendship> findAcceptedFriendships(@Param("userId") String userId);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.sender WHERE f.id.receiverId = :userId AND f.status = com.interacthub.entity.enums.FriendshipStatus.PENDING")
    List<Friendship> findPendingRequestsReceived(@Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE (f.id.senderId = :u1 AND f.id.receiverId = :u2) OR (f.id.senderId = :u2 AND f.id.receiverId = :u1)")
    boolean existsBetweenUsers(@Param("u1") String u1, @Param("u2") String u2);
}
