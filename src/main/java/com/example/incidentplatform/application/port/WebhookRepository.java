package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.webhook.Webhook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookRepository {

    Webhook save(Webhook webhook);

    Optional<Webhook> findById(UUID id);

    List<Webhook> findByTenantId(UUID tenantId);

    List<Webhook> findActiveByTenantId(UUID tenantId);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantId(UUID tenantId);
}
