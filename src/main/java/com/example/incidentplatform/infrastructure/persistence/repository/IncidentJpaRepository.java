package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for incident persistence.
 */
@Repository
public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, UUID> {

    /**
     * Find all incidents for a tenant.
     */
    List<IncidentEntity> findByTenantId(UUID tenantId);

    /**
     * Find incidents for a tenant with a specific status.
     */
    List<IncidentEntity> findByTenantIdAndStatus(UUID tenantId, String status);

    /**
     * Find incidents created by a specific user within a tenant.
     */
    List<IncidentEntity> findByTenantIdAndCreatedBy(UUID tenantId, UUID createdBy);

    /**
     * Check if an incident exists for a tenant.
     */
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
