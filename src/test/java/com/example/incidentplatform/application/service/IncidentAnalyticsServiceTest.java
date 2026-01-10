package com.example.incidentplatform.application.service;

import com.example.incidentplatform.api.dto.incident.IncidentAnalytics;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentAnalyticsServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private IncidentAnalyticsService analyticsService;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("generateAnalytics")
    class GenerateAnalytics {

        @Test
        @DisplayName("should return empty analytics when no incidents")
        void shouldReturnEmptyWhenNoIncidents() {
            // Given
            when(incidentRepository.findByTenantId(tenantId)).thenReturn(List.of());

            // When
            IncidentAnalytics analytics = analyticsService.generateAnalytics(tenantId);

            // Then
            assertThat(analytics.totalIncidents()).isZero();
            assertThat(analytics.openIncidents()).isZero();
        }

        @Test
        @DisplayName("should calculate total incidents correctly")
        void shouldCalculateTotalIncidents() {
            // Given
            List<Incident> incidents = List.of(
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null),
                    createIncident(IncidentStatus.IN_PROGRESS, Severity.MEDIUM, null),
                    createIncident(IncidentStatus.RESOLVED, Severity.LOW, Instant.now()));

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(incidents);

            // When
            IncidentAnalytics analytics = analyticsService.generateAnalytics(tenantId);

            // Then
            assertThat(analytics.totalIncidents()).isEqualTo(3);
            assertThat(analytics.openIncidents()).isEqualTo(2); // OPEN + IN_PROGRESS
            assertThat(analytics.resolvedIncidents()).isEqualTo(1);
        }

        @Test
        @DisplayName("should calculate severity distribution")
        void shouldCalculateSeverityDistribution() {
            // Given
            List<Incident> incidents = List.of(
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null),
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null),
                    createIncident(IncidentStatus.OPEN, Severity.LOW, null));

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(incidents);

            // When
            IncidentAnalytics analytics = analyticsService.generateAnalytics(tenantId);

            // Then
            assertThat(analytics.incidentsBySeverity()).containsEntry("HIGH", 2L);
            assertThat(analytics.incidentsBySeverity()).containsEntry("LOW", 1L);
        }

        @Test
        @DisplayName("should calculate average resolution time")
        void shouldCalculateAverageResolutionTime() {
            // Given
            Instant created = Instant.now().minus(24, ChronoUnit.HOURS);
            Instant resolved = Instant.now();

            Incident resolvedIncident = Incident.of(
                    UUID.randomUUID(), tenantId, "Title", "Desc",
                    Severity.HIGH, IncidentStatus.RESOLVED,
                    userId, created, created, resolved);

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(List.of(resolvedIncident));

            // When
            IncidentAnalytics analytics = analyticsService.generateAnalytics(tenantId);

            // Then
            assertThat(analytics.averageResolutionTimeHours()).isNotNull();
            assertThat(analytics.averageResolutionTimeHours()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("getSeverityDistribution")
    class GetSeverityDistribution {

        @Test
        @DisplayName("should return severity counts")
        void shouldReturnSeverityCounts() {
            // Given
            List<Incident> incidents = List.of(
                    createIncident(IncidentStatus.OPEN, Severity.CRITICAL, null),
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null),
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null));

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(incidents);

            // When
            Map<String, Long> distribution = analyticsService.getSeverityDistribution(tenantId);

            // Then
            assertThat(distribution).containsEntry("CRITICAL", 1L);
            assertThat(distribution).containsEntry("HIGH", 2L);
        }
    }

    @Nested
    @DisplayName("getMeanTimeToResolution")
    class GetMeanTimeToResolution {

        @Test
        @DisplayName("should return null when no resolved incidents")
        void shouldReturnNullWhenNoResolvedIncidents() {
            // Given
            List<Incident> incidents = List.of(
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null));

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(incidents);

            // When
            Double mttr = analyticsService.getMeanTimeToResolution(tenantId);

            // Then
            assertThat(mttr).isNull();
        }
    }

    @Nested
    @DisplayName("countIncidentsInRange")
    class CountIncidentsInRange {

        @Test
        @DisplayName("should count incidents in date range")
        void shouldCountIncidentsInRange() {
            // Given
            List<Incident> incidents = List.of(
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null),
                    createIncident(IncidentStatus.OPEN, Severity.LOW, null));

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(incidents);

            Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant end = Instant.now().plus(1, ChronoUnit.DAYS);

            // When
            long count = analyticsService.countIncidentsInRange(tenantId, start, end);

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getOpenIncidentRate")
    class GetOpenIncidentRate {

        @Test
        @DisplayName("should return 0 when no incidents")
        void shouldReturnZeroWhenNoIncidents() {
            // Given
            when(incidentRepository.findByTenantId(tenantId)).thenReturn(List.of());

            // When
            double rate = analyticsService.getOpenIncidentRate(tenantId);

            // Then
            assertThat(rate).isZero();
        }

        @Test
        @DisplayName("should calculate open rate correctly")
        void shouldCalculateOpenRateCorrectly() {
            // Given
            List<Incident> incidents = List.of(
                    createIncident(IncidentStatus.OPEN, Severity.HIGH, null),
                    createIncident(IncidentStatus.RESOLVED, Severity.LOW, Instant.now()),
                    createIncident(IncidentStatus.RESOLVED, Severity.LOW, Instant.now()),
                    createIncident(IncidentStatus.RESOLVED, Severity.LOW, Instant.now()));

            when(incidentRepository.findByTenantId(tenantId)).thenReturn(incidents);

            // When
            double rate = analyticsService.getOpenIncidentRate(tenantId);

            // Then
            assertThat(rate).isEqualTo(25.0); // 1 out of 4 = 25%
        }
    }

    private Incident createIncident(IncidentStatus status, Severity severity, Instant resolvedAt) {
        Instant now = Instant.now();
        return Incident.of(
                UUID.randomUUID(), tenantId, "Title", "Description",
                severity, status, userId, now, now, resolvedAt);
    }
}
