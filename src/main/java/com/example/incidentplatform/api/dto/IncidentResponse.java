package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;

import java.time.Instant;
import java.util.UUID;


public record IncidentResponse(
        UUID id,
        UUID tenantId,
        String title,
        String description,
        Severity severity,
        IncidentStatus status,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt) {
}
