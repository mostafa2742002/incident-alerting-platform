package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.Severity;
import java.util.UUID;

/**
 * Request DTO for creating an incident.
 */
public record CreateIncidentRequest(
        String title,
        String description,
        Severity severity) {
}
