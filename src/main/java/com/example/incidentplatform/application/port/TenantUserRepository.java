package com.example.incidentplatform.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.incidentplatform.domain.model.tenant.TenantUser;


public interface TenantUserRepository {


    TenantUser save(TenantUser tenantUser);

    Optional<TenantUser> findById(UUID id);

    Optional<TenantUser> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    List<TenantUser> findByTenantId(UUID tenantId);

    List<TenantUser> findByUserId(UUID userId);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    void delete(TenantUser tenantUser);

    void deleteByTenantIdAndUserId(UUID tenantId, UUID userId);
}
