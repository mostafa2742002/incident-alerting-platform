package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.WebhookDeliveryRepository;
import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;
import com.example.incidentplatform.infrastructure.persistence.mapper.WebhookDeliveryMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.WebhookDeliveryJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaWebhookDeliveryRepositoryAdapter implements WebhookDeliveryRepository {

    private final WebhookDeliveryJpaRepository jpaRepository;
    private final WebhookDeliveryMapper mapper;

    public JpaWebhookDeliveryRepositoryAdapter(WebhookDeliveryJpaRepository jpaRepository,
            WebhookDeliveryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public WebhookDelivery save(WebhookDelivery delivery) {
        var entity = mapper.toEntity(delivery);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<WebhookDelivery> findByWebhookId(UUID webhookId) {
        return jpaRepository.findByWebhookId(webhookId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WebhookDelivery> findRecentByWebhookId(UUID webhookId, int limit) {
        return jpaRepository.findByWebhookIdOrderByDeliveredAtDesc(webhookId, PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countSuccessByWebhookId(UUID webhookId) {
        return jpaRepository.countByWebhookIdAndSuccess(webhookId, true);
    }

    @Override
    public long countFailuresByWebhookId(UUID webhookId) {
        return jpaRepository.countByWebhookIdAndSuccess(webhookId, false);
    }

    @Override
    @Transactional
    public int deleteOldDeliveries(Instant cutoff) {
        return jpaRepository.deleteOldDeliveries(cutoff);
    }
}
