package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.WebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookJpaRepository extends JpaRepository<WebhookEntity, UUID> {

    List<WebhookEntity> findByTenantId(UUID tenantId);

    List<WebhookEntity> findByTenantIdAndIsActiveTrue(UUID tenantId);

    long countByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
