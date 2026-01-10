package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;
import com.example.incidentplatform.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.stereotype.Component;


@Component
public class IncidentMapper {

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
