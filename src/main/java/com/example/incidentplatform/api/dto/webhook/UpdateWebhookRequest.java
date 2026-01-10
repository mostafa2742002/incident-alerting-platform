package com.example.incidentplatform.api.dto.webhook;

import com.example.incidentplatform.domain.model.webhook.WebhookEventType;

import java.util.Set;

public record UpdateWebhookRequest(
        String name,
        String url,
        String secret,
        Set<WebhookEventType> events) {
}
