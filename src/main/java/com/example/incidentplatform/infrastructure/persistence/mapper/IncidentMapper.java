package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
import com.example.incidentplatform.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between Incident domain model and IncidentEntity JPA entity.
 */
@Component
public class IncidentMapper {

    /**
     * Convert domain model to JPA entity for persistence.
     */
    public IncidentEntity toEntity(Incident incident) {
        return new IncidentEntity(
                incident.id(),
                incident.tenantId(),
                incident.title(),
                incident.description(),
                incident.severity().name(),
                incident.status().name(),
                incident.createdBy(),
                incident.createdAt(),
                incident.updatedAt(),
                incident.resolvedAt());
    }

    /**
     * Convert JPA entity to domain model.
     */
    public Incident toDomain(IncidentEntity entity) {
        return Incident.of(
                entity.getId(),
                entity.getTenantId(),
                entity.getTitle(),
                entity.getDescription(),
                Severity.valueOf(entity.getSeverity()),
                IncidentStatus.valueOf(entity.getStatus()),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getResolvedAt());
    }
}
