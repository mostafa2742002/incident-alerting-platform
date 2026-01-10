package com.example.incidentplatform.domain.model.webhook;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Domain model for Webhook configuration.
 */
public record Webhook(
        UUID id,
        UUID tenantId,
        String name,
        String url,
        String secret,
        Set<WebhookEventType> events,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        Instant lastTriggeredAt,
        int failureCount) {
    /**
     * Create a new webhook registration.
     */
    public static Webhook createNew(UUID tenantId, String name, String url, String secret,
            Set<WebhookEventType> events) {
        Instant now = Instant.now();
        return new Webhook(
                UUID.randomUUID(),
                tenantId,
                name,
                url,
                secret,
                events,
                true,
                now,
                now,
                null,
                0);
    }

    /**
     * Reconstruct from persistence.
     */
    public static Webhook of(UUID id, UUID tenantId, String name, String url, String secret,
            Set<WebhookEventType> events, boolean isActive, Instant createdAt,
            Instant updatedAt, Instant lastTriggeredAt, int failureCount) {
        return new Webhook(id, tenantId, name, url, secret, events, isActive, createdAt, updatedAt, lastTriggeredAt,
                failureCount);
    }

    /**
     * Check if this webhook is subscribed to an event type.
     */
    public boolean isSubscribedTo(WebhookEventType eventType) {
        return events.contains(eventType);
    }

    /**
     * Update webhook configuration.
     */
    public Webhook update(String name, String url, String secret, Set<WebhookEventType> events) {
        return new Webhook(
                this.id,
                this.tenantId,
                name != null ? name : this.name,
                url != null ? url : this.url,
                secret != null ? secret : this.secret,
                events != null ? events : this.events,
                this.isActive,
                this.createdAt,
                Instant.now(),
                this.lastTriggeredAt,
                this.failureCount);
    }

    /**
     * Activate or deactivate the webhook.
     */
    public Webhook setActive(boolean active) {
        return new Webhook(
                this.id, this.tenantId, this.name, this.url, this.secret,
                this.events, active, this.createdAt, Instant.now(),
                this.lastTriggeredAt, this.failureCount);
    }

    /**
     * Record a successful trigger.
     */
    public Webhook recordSuccess() {
        return new Webhook(
                this.id, this.tenantId, this.name, this.url, this.secret,
                this.events, this.isActive, this.createdAt, Instant.now(),
                Instant.now(), 0);
    }

    /**
     * Record a failed trigger.
     */
    public Webhook recordFailure() {
        return new Webhook(
                this.id, this.tenantId, this.name, this.url, this.secret,
                this.events, this.isActive, this.createdAt, Instant.now(),
                this.lastTriggeredAt, this.failureCount + 1);
    }

    /**
     * Check if webhook should be disabled due to too many failures.
     */
    public boolean shouldDisable(int maxFailures) {
        return failureCount >= maxFailures;
    }
}
