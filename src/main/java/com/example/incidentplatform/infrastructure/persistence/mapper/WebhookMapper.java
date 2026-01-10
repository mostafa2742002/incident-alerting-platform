package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.webhook.Webhook;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;
import com.example.incidentplatform.infrastructure.persistence.entity.WebhookEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebhookMapper {

    public WebhookEntity toEntity(Webhook webhook) {
        String eventsString = webhook.events().stream()
                .map(WebhookEventType::name)
                .collect(Collectors.joining(","));

        return new WebhookEntity(
                webhook.id(),
                webhook.tenantId(),
                webhook.name(),
                webhook.url(),
                webhook.secret(),
                eventsString,
                webhook.isActive(),
                webhook.createdAt(),
                webhook.updatedAt(),
                webhook.lastTriggeredAt(),
                webhook.failureCount());
    }

    public Webhook toDomain(WebhookEntity entity) {
        Set<WebhookEventType> events = Arrays.stream(entity.getEvents().split(","))
                .filter(s -> !s.isBlank())
                .map(WebhookEventType::valueOf)
                .collect(Collectors.toSet());

        return Webhook.of(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getUrl(),
                entity.getSecret(),
                events,
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastTriggeredAt(),
                entity.getFailureCount());
    }
}
