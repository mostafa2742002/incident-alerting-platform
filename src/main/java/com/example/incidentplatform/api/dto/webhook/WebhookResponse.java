package com.example.incidentplatform.api.dto.webhook;

import com.example.incidentplatform.domain.model.webhook.Webhook;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record WebhookResponse(
        UUID id,
        UUID tenantId,
        String name,
        String url,
        boolean hasSecret,
        Set<WebhookEventType> events,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        Instant lastTriggeredAt,
        int failureCount) {
    public static WebhookResponse from(Webhook webhook) {
        return new WebhookResponse(
                webhook.id(),
                webhook.tenantId(),
                webhook.name(),
                webhook.url(),
                webhook.secret() != null && !webhook.secret().isBlank(),
                webhook.events(),
                webhook.isActive(),
                webhook.createdAt(),
                webhook.updatedAt(),
                webhook.lastTriggeredAt(),
                webhook.failureCount());
    }
}
