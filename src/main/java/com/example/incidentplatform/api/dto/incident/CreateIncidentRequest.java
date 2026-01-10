package com.example.incidentplatform.api.dto.incident;

import java.util.UUID;

import com.example.incidentplatform.domain.model.incident.Severity;

public record CreateIncidentRequest(
        String title,
        String description,
        Severity severity) {
}
