package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for an incident.
 */
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
