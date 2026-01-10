package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryRepository {

    WebhookDelivery save(WebhookDelivery delivery);

    List<WebhookDelivery> findByWebhookId(UUID webhookId);

    List<WebhookDelivery> findRecentByWebhookId(UUID webhookId, int limit);

    long countSuccessByWebhookId(UUID webhookId);

    long countFailuresByWebhookId(UUID webhookId);

    int deleteOldDeliveries(Instant cutoff);
}
