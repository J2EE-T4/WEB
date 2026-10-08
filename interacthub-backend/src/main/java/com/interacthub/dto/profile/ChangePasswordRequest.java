package com.interacthub.dto.profile;
import jakarta.validation.constraints.*;
public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min=6) String newPassword, @NotBlank String confirmPassword) {}
