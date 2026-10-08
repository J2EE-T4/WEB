package com.interacthub.dto.auth;
import jakarta.validation.constraints.*;
public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
