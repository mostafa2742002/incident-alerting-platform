package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.TenantUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantUserJpaRepository extends JpaRepository<TenantUserEntity, UUID> {

    Optional<TenantUserEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    List<TenantUserEntity> findByTenantId(UUID tenantId);

    List<TenantUserEntity> findByUserId(UUID userId);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    void deleteByTenantIdAndUserId(UUID tenantId, UUID userId);
}
