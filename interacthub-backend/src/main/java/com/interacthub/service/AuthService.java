package com.interacthub.service;

import com.interacthub.dto.auth.*;
import com.interacthub.entity.RefreshToken;
import com.interacthub.entity.User;
import com.interacthub.exception.*;
import com.interacthub.repository.UserRepository;
import com.interacthub.security.JwtTokenProvider;
import com.interacthub.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already in use");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName() != null && !req.displayName().isBlank() ? req.displayName() : req.username())
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("User registered successfully: {}", user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                accessToken,
                refreshToken.getToken(),
                user.getRole(),
                "Bearer",
                jwtTokenProvider.getExpirationMs()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                accessToken,
                refreshToken.getToken(),
                user.getRole(),
                "Bearer",
                jwtTokenProvider.getExpirationMs()
        );
    }

    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest req) {
        RefreshToken refreshToken = refreshTokenService.findByToken(req.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new TokenRefreshResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                jwtTokenProvider.getExpirationMs()
        );
    }

    @Transactional
    public void logout(String userId, String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            long remainingMs = jwtTokenProvider.getRemainingTimeMs(jwt);
            if (remainingMs > 0) {
                tokenBlacklistService.blacklistToken(jwt, System.currentTimeMillis() + remainingMs);
            }
        }
        if (userId != null) {
            refreshTokenService.revokeAllUserTokens(userId);
            log.info("User {} logged out, refresh tokens revoked and access token blacklisted", userId);
        }
    }
}
