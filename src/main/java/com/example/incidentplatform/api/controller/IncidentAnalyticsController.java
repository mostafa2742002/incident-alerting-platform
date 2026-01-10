package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.incident.IncidentAnalytics;
import com.example.incidentplatform.application.service.IncidentAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/incidents/analytics")
public class IncidentAnalyticsController {

    private final IncidentAnalyticsService analyticsService;

    public IncidentAnalyticsController(IncidentAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get comprehensive analytics dashboard data.
     */
    @GetMapping
    public ResponseEntity<IncidentAnalytics> getAnalytics(@PathVariable UUID tenantId) {
        IncidentAnalytics analytics = analyticsService.generateAnalytics(tenantId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get severity distribution.
     */
    @GetMapping("/severity-distribution")
    public ResponseEntity<Map<String, Long>> getSeverityDistribution(@PathVariable UUID tenantId) {
        Map<String, Long> distribution = analyticsService.getSeverityDistribution(tenantId);
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get mean time to resolution (MTTR).
     */
    @GetMapping("/mttr")
    public ResponseEntity<Map<String, Object>> getMTTR(@PathVariable UUID tenantId) {
        Double mttr = analyticsService.getMeanTimeToResolution(tenantId);
        return ResponseEntity.ok(Map.of(
                "mttrHours", mttr != null ? mttr : "N/A",
                "description", "Mean Time To Resolution in hours"));
    }

    /**
     * Get incidents count for a date range.
     */
    @GetMapping("/count-in-range")
    public ResponseEntity<Map<String, Object>> getCountInRange(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        long count = analyticsService.countIncidentsInRange(tenantId, start, end);
        return ResponseEntity.ok(Map.of(
                "count", count,
                "start", start.toString(),
                "end", end.toString()));
    }

    /**
     * Get open incident rate (percentage).
     */
    @GetMapping("/open-rate")
    public ResponseEntity<Map<String, Object>> getOpenRate(@PathVariable UUID tenantId) {
        double rate = analyticsService.getOpenIncidentRate(tenantId);
        return ResponseEntity.ok(Map.of(
                "openRatePercent", rate,
                "description", "Percentage of incidents currently open or in-progress"));
    }

    /**
     * Get incidents grouped by creator.
     */
    @GetMapping("/by-creator")
    public ResponseEntity<Map<UUID, Long>> getIncidentsByCreator(@PathVariable UUID tenantId) {
        Map<UUID, Long> byCreator = analyticsService.getIncidentsByCreator(tenantId);
        return ResponseEntity.ok(byCreator);
    }
}
