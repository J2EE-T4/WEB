package com.interacthub.service;

import com.interacthub.entity.RefreshToken;
import com.interacthub.entity.User;
import com.interacthub.exception.ResourceNotFoundException;
import com.interacthub.exception.UnauthorizedException;
import com.interacthub.repository.RefreshTokenRepository;
import com.interacthub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // Default 7 days
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Revoke existing refresh tokens for this user for security (token rotation)
        refreshTokenRepository.revokeAllByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .token(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token was expired or revoked. Please sign in again.");
        }
        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Scheduled(fixedRate = 86400000) // Cleanup daily
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
        log.info("Expired and revoked refresh tokens purged.");
    }
}
