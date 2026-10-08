package com.interacthub.dto.post;
import jakarta.validation.constraints.*;
public record ReportPostRequest(@NotBlank @Size(max = 500) String reason) {}
