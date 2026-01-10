package com.example.incidentplatform.application.service;

import com.example.incidentplatform.api.dto.incident.IncidentAnalytics;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IncidentAnalyticsService {

    private final IncidentRepository incidentRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public IncidentAnalyticsService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /**
     * Generate comprehensive analytics for a tenant's incidents.
     */
    public IncidentAnalytics generateAnalytics(UUID tenantId) {
        List<Incident> incidents = incidentRepository.findByTenantId(tenantId);

        if (incidents.isEmpty()) {
            return IncidentAnalytics.empty();
        }

        long totalIncidents = incidents.size();

        // Count by status
        Map<IncidentStatus, Long> byStatus = incidents.stream()
                .collect(Collectors.groupingBy(Incident::status, Collectors.counting()));

        long openIncidents = byStatus.getOrDefault(IncidentStatus.OPEN, 0L)
                + byStatus.getOrDefault(IncidentStatus.IN_PROGRESS, 0L);
        long resolvedIncidents = byStatus.getOrDefault(IncidentStatus.RESOLVED, 0L);
        long closedIncidents = byStatus.getOrDefault(IncidentStatus.CLOSED, 0L);

        // Convert status map to string keys
        Map<String, Long> incidentsByStatus = byStatus.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        // Count by severity
        Map<String, Long> incidentsBySeverity = incidents.stream()
                .collect(Collectors.groupingBy(i -> i.severity().name(), Collectors.counting()));

        // Calculate average resolution time (only for resolved/closed incidents with
        // resolvedAt)
        Double avgResolutionTimeHours = calculateAverageResolutionTime(incidents);

        // Incidents created per day (last 30 days)
        Map<String, Long> createdByDay = getIncidentsCreatedByDay(incidents, 30);

        // Incidents resolved per day (last 30 days)
        Map<String, Long> resolvedByDay = getIncidentsResolvedByDay(incidents, 30);

        // Weekly and monthly counts
        Instant now = Instant.now();
        Instant weekAgo = now.minus(7, ChronoUnit.DAYS);
        Instant monthAgo = now.minus(30, ChronoUnit.DAYS);

        long createdThisWeek = incidents.stream()
                .filter(i -> i.createdAt().isAfter(weekAgo))
                .count();

        long resolvedThisWeek = incidents.stream()
                .filter(i -> i.resolvedAt() != null && i.resolvedAt().isAfter(weekAgo))
                .count();

        long createdThisMonth = incidents.stream()
                .filter(i -> i.createdAt().isAfter(monthAgo))
                .count();

        long resolvedThisMonth = incidents.stream()
                .filter(i -> i.resolvedAt() != null && i.resolvedAt().isAfter(monthAgo))
                .count();

        return new IncidentAnalytics(
                totalIncidents,
                openIncidents,
                resolvedIncidents,
                closedIncidents,
                avgResolutionTimeHours,
                incidentsBySeverity,
                incidentsByStatus,
                createdByDay,
                resolvedByDay,
                createdThisWeek,
                resolvedThisWeek,
                createdThisMonth,
                resolvedThisMonth);
    }

    /**
     * Calculate average resolution time in hours for incidents that have been
     * resolved.
     */
    private Double calculateAverageResolutionTime(List<Incident> incidents) {
        List<Long> resolutionTimes = incidents.stream()
                .filter(i -> i.resolvedAt() != null)
                .map(i -> Duration.between(i.createdAt(), i.resolvedAt()).toHours())
                .toList();

        if (resolutionTimes.isEmpty()) {
            return null;
        }

        double sum = resolutionTimes.stream().mapToLong(Long::longValue).sum();
        return Math.round((sum / resolutionTimes.size()) * 100.0) / 100.0;
    }

    /**
     * Get count of incidents created per day for the last N days.
     */
    private Map<String, Long> getIncidentsCreatedByDay(List<Incident> incidents, int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        return incidents.stream()
                .filter(i -> i.createdAt().isAfter(cutoff))
                .collect(Collectors.groupingBy(
                        i -> formatDate(i.createdAt()),
                        TreeMap::new,
                        Collectors.counting()));
    }

    /**
     * Get count of incidents resolved per day for the last N days.
     */
    private Map<String, Long> getIncidentsResolvedByDay(List<Incident> incidents, int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        return incidents.stream()
                .filter(i -> i.resolvedAt() != null && i.resolvedAt().isAfter(cutoff))
                .collect(Collectors.groupingBy(
                        i -> formatDate(i.resolvedAt()),
                        TreeMap::new,
                        Collectors.counting()));
    }

    private String formatDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneOffset.UTC).format(DATE_FORMATTER);
    }

    /**
     * Get severity distribution statistics.
     */
    public Map<String, Long> getSeverityDistribution(UUID tenantId) {
        List<Incident> incidents = incidentRepository.findByTenantId(tenantId);
        return incidents.stream()
                .collect(Collectors.groupingBy(i -> i.severity().name(), Collectors.counting()));
    }

    /**
     * Get mean time to resolution (MTTR) in hours.
     */
    public Double getMeanTimeToResolution(UUID tenantId) {
        List<Incident> incidents = incidentRepository.findByTenantId(tenantId);
        return calculateAverageResolutionTime(incidents);
    }

    /**
     * Get incidents created in a date range.
     */
    public long countIncidentsInRange(UUID tenantId, Instant start, Instant end) {
        List<Incident> incidents = incidentRepository.findByTenantId(tenantId);
        return incidents.stream()
                .filter(i -> !i.createdAt().isBefore(start) && !i.createdAt().isAfter(end))
                .count();
    }

    /**
     * Get open incident rate (percentage of incidents still open/in-progress).
     */
    public double getOpenIncidentRate(UUID tenantId) {
        List<Incident> incidents = incidentRepository.findByTenantId(tenantId);
        if (incidents.isEmpty()) {
            return 0.0;
        }

        long openCount = incidents.stream()
                .filter(i -> i.status() == IncidentStatus.OPEN || i.status() == IncidentStatus.IN_PROGRESS)
                .count();

        return Math.round((openCount * 100.0 / incidents.size()) * 100.0) / 100.0;
    }

    /**
     * Get incidents grouped by creator.
     */
    public Map<UUID, Long> getIncidentsByCreator(UUID tenantId) {
        List<Incident> incidents = incidentRepository.findByTenantId(tenantId);
        return incidents.stream()
                .filter(i -> i.createdBy() != null)
                .collect(Collectors.groupingBy(Incident::createdBy, Collectors.counting()));
    }
}
