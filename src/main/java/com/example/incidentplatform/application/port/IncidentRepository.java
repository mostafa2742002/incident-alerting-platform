package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.Incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) for incident persistence.
 * Abstracts the persistence mechanism from the application layer.
 */
public interface IncidentRepository {

    /**
     * Save or update an incident.
     */
    Incident save(Incident incident);

    /**
     * Find an incident by ID and tenant ID.
     */
    Optional<Incident> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * List all incidents for a tenant.
     */
    List<Incident> findByTenantId(UUID tenantId);

    /**
     * List incidents for a tenant with a specific status.
     */
    List<Incident> findByTenantIdAndStatus(UUID tenantId, String status);

    /**
     * Delete an incident.
     */
    void deleteByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Check if an incident exists for a tenant.
     */
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
