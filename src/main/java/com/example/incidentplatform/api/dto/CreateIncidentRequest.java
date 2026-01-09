package com.example.incidentplatform.api.dto;

import com.example.incidentplatform.domain.model.Severity;
import java.util.UUID;

public record CreateIncidentRequest(
        String title,
        String description,
        Severity severity) {
}
