package com.example.incidentplatform.domain.model.webhook;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model for a webhook delivery record.
 */
public record WebhookDelivery(
        UUID id,
        UUID webhookId,
        WebhookEventType eventType,
        Map<String, Object> payload,
        Integer responseStatus,
        String responseBody,
        Instant deliveredAt,
        boolean success,
        String errorMessage) {
    /**
     * Create a successful delivery record.
     */
    public static WebhookDelivery success(UUID webhookId, WebhookEventType eventType,
            Map<String, Object> payload, int responseStatus, String responseBody) {
        return new WebhookDelivery(
                UUID.randomUUID(),
                webhookId,
                eventType,
                payload,
                responseStatus,
                responseBody,
                Instant.now(),
                true,
                null);
    }

    /**
     * Create a failed delivery record.
     */
    public static WebhookDelivery failure(UUID webhookId, WebhookEventType eventType,
            Map<String, Object> payload, String errorMessage) {
        return new WebhookDelivery(
                UUID.randomUUID(),
                webhookId,
                eventType,
                payload,
                null,
                null,
                Instant.now(),
                false,
                errorMessage);
    }

    /**
     * Reconstruct from persistence.
     */
    public static WebhookDelivery of(UUID id, UUID webhookId, WebhookEventType eventType,
            Map<String, Object> payload, Integer responseStatus,
            String responseBody, Instant deliveredAt, boolean success, String errorMessage) {
        return new WebhookDelivery(id, webhookId, eventType, payload, responseStatus, responseBody, deliveredAt,
                success, errorMessage);
    }
}
