package com.interacthub.repository;

import com.interacthub.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId")
    void revokeAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now OR r.revoked = true")
    void deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
}
