package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface IncidentJpaRepository extends JpaRepository<IncidentEntity, UUID> {


    List<IncidentEntity> findByTenantId(UUID tenantId);


    List<IncidentEntity> findByTenantIdAndStatus(UUID tenantId, String status);


    List<IncidentEntity> findByTenantIdAndCreatedBy(UUID tenantId, UUID createdBy);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
