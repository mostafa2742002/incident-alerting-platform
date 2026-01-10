package com.example.incidentplatform.api.dto.incident;

import java.util.Map;

/**
 * Analytics data for incidents in a tenant.
 */
public record IncidentAnalytics(
        long totalIncidents,
        long openIncidents,
        long resolvedIncidents,
        long closedIncidents,
        Double averageResolutionTimeHours,
        Map<String, Long> incidentsBySeverity,
        Map<String, Long> incidentsByStatus,
        Map<String, Long> incidentsCreatedByDay,
        Map<String, Long> incidentsResolvedByDay,
        long incidentsCreatedThisWeek,
        long incidentsResolvedThisWeek,
        long incidentsCreatedThisMonth,
        long incidentsResolvedThisMonth) {
    public static IncidentAnalytics empty() {
        return new IncidentAnalytics(
                0, 0, 0, 0, null,
                Map.of(), Map.of(), Map.of(), Map.of(),
                0, 0, 0, 0);
    }
}
