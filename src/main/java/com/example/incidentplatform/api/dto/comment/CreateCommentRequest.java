package com.example.incidentplatform.api.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateCommentRequest(
        @NotBlank(message = "Content is required") 
        @Size(max = 10000, message = "Content must not exceed 10000 characters") 
        String content
    ) {
}
