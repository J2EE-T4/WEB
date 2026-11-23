package com.interacthub.dto.auth;

public record AuthResponse(
    String userId,
    String username,
    String email,
    String displayName,
    String avatarUrl,
    String token,
    String refreshToken,
    String role,
    String tokenType,
    long expiresIn
) {
    public AuthResponse(String userId, String username, String email, String displayName, String avatarUrl, String token, String role) {
        this(userId, username, email, displayName, avatarUrl, token, null, role, "Bearer", 86400000L);
    }

    public AuthResponse(String userId, String username, String email, String displayName, String avatarUrl, String token, String refreshToken, String role) {
        this(userId, username, email, displayName, avatarUrl, token, refreshToken, role, "Bearer", 86400000L);
    }
}
