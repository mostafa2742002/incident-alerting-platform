package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.IncidentComment;

import java.time.Instant;
import java.util.UUID;


public record CommentResponse(
        UUID id,
        UUID incidentId,
        UUID authorId,
        String content,
        Instant createdAt,
        Instant updatedAt) {

    public static CommentResponse from(IncidentComment comment) {
        return new CommentResponse(
                comment.id(),
                comment.incidentId(),
                comment.authorId(),
                comment.content(),
                comment.createdAt(),
                comment.updatedAt());
    }
}
