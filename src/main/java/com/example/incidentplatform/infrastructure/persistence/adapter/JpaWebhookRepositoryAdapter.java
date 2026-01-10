package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.WebhookRepository;
import com.example.incidentplatform.domain.model.webhook.Webhook;
import com.example.incidentplatform.infrastructure.persistence.mapper.WebhookMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.WebhookJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaWebhookRepositoryAdapter implements WebhookRepository {

    private final WebhookJpaRepository jpaRepository;
    private final WebhookMapper mapper;

    public JpaWebhookRepositoryAdapter(WebhookJpaRepository jpaRepository, WebhookMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Webhook save(Webhook webhook) {
        var entity = mapper.toEntity(webhook);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Webhook> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Webhook> findByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Webhook> findActiveByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return jpaRepository.countByTenantId(tenantId);
    }
}
