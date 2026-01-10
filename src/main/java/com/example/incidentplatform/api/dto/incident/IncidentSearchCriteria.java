package com.example.incidentplatform.api.dto.incident;

import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;

import java.time.Instant;


public record IncidentSearchCriteria(
        String searchTerm,
        IncidentStatus status,
        Severity severity,
        Instant createdAfter,
        Instant createdBefore, 
        Boolean resolved, 
        String sortBy, 
        String sortDirection 
) {
    public IncidentSearchCriteria {
        // Default sort direction
        if (sortDirection == null) {
            sortDirection = "DESC";
        }
        if (sortBy == null) {
            sortBy = "createdAt";
        }
    }

    public static IncidentSearchCriteria empty() {
        return new IncidentSearchCriteria(null, null, null, null, null, null, null, null);
    }

    public boolean hasFilters() {
        return searchTerm != null || status != null || severity != null ||
                createdAfter != null || createdBefore != null || resolved != null;
    }
}
