package com.example.incidentplatform.api.dto.notification;

import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType type,
        String title,
        String message,
        UUID incidentId,
        boolean read,
        Instant readAt,
        Instant createdAt,
        Map<String, Object> metadata) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.userId(),
                notification.type(),
                notification.title(),
                notification.message(),
                notification.incidentId(),
                notification.isRead(),
                notification.readAt(),
                notification.createdAt(),
                notification.metadata());
    }
}
