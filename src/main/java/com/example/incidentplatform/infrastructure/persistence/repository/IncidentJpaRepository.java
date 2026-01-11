package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentJpaRepository
                extends JpaRepository<IncidentEntity, UUID>, JpaSpecificationExecutor<IncidentEntity> {

        List<IncidentEntity> findByTenantId(UUID tenantId);

        List<IncidentEntity> findByTenantIdAndStatus(UUID tenantId, String status);

        List<IncidentEntity> findByTenantIdAndCreatedBy(UUID tenantId, UUID createdBy);

        List<IncidentEntity> findByTenantIdAndSeverity(UUID tenantId, String severity);

        boolean existsByIdAndTenantId(UUID id, UUID tenantId);

        long countByTenantId(UUID tenantId);

        long countByTenantIdAndStatus(UUID tenantId, String status);

        @Query(value = """
                        SELECT * FROM incidents i WHERE i.tenant_id = :tenantId
                        AND (:searchTerm IS NULL OR :searchTerm = '' OR LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                            OR LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
                        AND (CAST(:status AS VARCHAR) IS NULL OR i.status = :status)
                        AND (CAST(:severity AS VARCHAR) IS NULL OR i.severity = :severity)
                        AND (CAST(:createdAfter AS TIMESTAMP) IS NULL OR i.created_at >= :createdAfter)
                        AND (CAST(:createdBefore AS TIMESTAMP) IS NULL OR i.created_at <= :createdBefore)
                        AND (:resolved IS NULL OR (:resolved = true AND i.resolved_at IS NOT NULL)
                            OR (:resolved = false AND i.resolved_at IS NULL))
                        ORDER BY i.created_at DESC
                        """, nativeQuery = true)
        List<IncidentEntity> searchIncidents(
                        @Param("tenantId") UUID tenantId,
                        @Param("searchTerm") String searchTerm,
                        @Param("status") String status,
                        @Param("severity") String severity,
                        @Param("createdAfter") Instant createdAfter,
                        @Param("createdBefore") Instant createdBefore,
                        @Param("resolved") Boolean resolved);
}
