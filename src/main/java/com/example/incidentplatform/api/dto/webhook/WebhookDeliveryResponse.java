package com.example.incidentplatform.api.dto.webhook;

import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WebhookDeliveryResponse(
        UUID id,
        UUID webhookId,
        WebhookEventType eventType,
        Map<String, Object> payload,
        Integer responseStatus,
        String responseBody,
        Instant deliveredAt,
        boolean success,
        String errorMessage) {
    public static WebhookDeliveryResponse from(WebhookDelivery delivery) {
        return new WebhookDeliveryResponse(
                delivery.id(),
                delivery.webhookId(),
                delivery.eventType(),
                delivery.payload(),
                delivery.responseStatus(),
                delivery.responseBody(),
                delivery.deliveredAt(),
                delivery.success(),
                delivery.errorMessage());
    }
}
