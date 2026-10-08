package com.interacthub.dto.comment;
import jakarta.validation.constraints.*;
public record CommentRequest(@NotBlank String postId, @NotBlank @Size(max = 2000) String content) {}
