package com.interacthub.dto.interaction;
import jakarta.validation.constraints.*;
public record LikeRequest(@NotBlank String postId, @NotBlank String userId) {}
