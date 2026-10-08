package com.interacthub.dto.auth;
import jakarta.validation.constraints.*;
public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank @Size(min = 6, max = 100) String password,
    @Size(max = 100) String displayName
) {}
