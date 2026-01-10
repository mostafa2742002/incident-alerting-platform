package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.Incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository {

    Incident save(Incident incident);

    Optional<Incident> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Incident> findByTenantId(UUID tenantId);

    List<Incident> findByTenantIdAndStatus(UUID tenantId, String status);

    void deleteByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsById(UUID id);
}
