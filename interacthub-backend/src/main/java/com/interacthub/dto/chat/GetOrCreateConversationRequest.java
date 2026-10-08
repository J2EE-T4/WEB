package com.interacthub.dto.chat;
import jakarta.validation.constraints.NotBlank;
public record GetOrCreateConversationRequest(@NotBlank String otherUserId) {}
