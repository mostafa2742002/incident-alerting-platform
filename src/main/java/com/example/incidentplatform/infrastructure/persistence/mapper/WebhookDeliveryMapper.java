package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;
import com.example.incidentplatform.infrastructure.persistence.entity.WebhookDeliveryEntity;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryMapper {

    public WebhookDeliveryEntity toEntity(WebhookDelivery delivery) {
        return new WebhookDeliveryEntity(
                delivery.id(),
                delivery.webhookId(),
                delivery.eventType().name(),
                delivery.payload(),
                delivery.responseStatus(),
                delivery.responseBody(),
                delivery.deliveredAt(),
                delivery.success(),
                delivery.errorMessage());
    }

    public WebhookDelivery toDomain(WebhookDeliveryEntity entity) {
        return WebhookDelivery.of(
                entity.getId(),
                entity.getWebhookId(),
                WebhookEventType.valueOf(entity.getEventType()),
                entity.getPayload(),
                entity.getResponseStatus(),
                entity.getResponseBody(),
                entity.getDeliveredAt(),
                entity.isSuccess(),
                entity.getErrorMessage());
    }
}
