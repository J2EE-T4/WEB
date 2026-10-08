package com.interacthub.dto.auth;
public record AuthResponse(String userId, String username, String email, String displayName, String avatarUrl, String token, String role) {}
