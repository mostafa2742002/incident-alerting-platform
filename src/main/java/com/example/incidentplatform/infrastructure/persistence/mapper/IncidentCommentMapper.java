package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.incident.IncidentComment;
import com.example.incidentplatform.infrastructure.persistence.entity.IncidentCommentEntity;
import org.springframework.stereotype.Component;


@Component
public class IncidentCommentMapper {

    public IncidentCommentEntity toEntity(IncidentComment comment) {
        if (comment == null) {
            return null;
        }
        return new IncidentCommentEntity(
                comment.id(),
                comment.incidentId(),
                comment.authorId(),
                comment.content(),
                comment.createdAt(),
                comment.updatedAt());
    }

    public IncidentComment toDomain(IncidentCommentEntity entity) {
        if (entity == null) {
            return null;
        }
        return IncidentComment.of(
                entity.getId(),
                entity.getIncidentId(),
                entity.getAuthorId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
