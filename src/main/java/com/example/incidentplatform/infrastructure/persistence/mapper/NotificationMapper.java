package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;
import com.example.incidentplatform.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationEntity toEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationEntity(
                notification.id(),
                notification.userId(),
                notification.type().name(),
                notification.title(),
                notification.message(),
                notification.incidentId(),
                notification.readAt(),
                notification.createdAt(),
                notification.metadata());
    }

    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Notification.of(
                entity.getId(),
                entity.getUserId(),
                NotificationType.valueOf(entity.getType()),
                entity.getTitle(),
                entity.getMessage(),
                entity.getIncidentId(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getMetadata());
    }
}
