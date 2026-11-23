package com.interacthub.dto.auth;

public record TokenRefreshResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {
    public TokenRefreshResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
