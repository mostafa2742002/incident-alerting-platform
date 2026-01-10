package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.incident.IncidentAnalytics;
import com.example.incidentplatform.application.service.IncidentAnalyticsService;
import com.example.incidentplatform.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IncidentAnalyticsControllerTest {

    @Mock
    private IncidentAnalyticsService analyticsService;

    @InjectMocks
    private IncidentAnalyticsController controller;

    private MockMvc mockMvc;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        tenantId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/incidents/analytics")
    class GetAnalytics {

        @Test
        @DisplayName("should return comprehensive analytics")
        void shouldReturnComprehensiveAnalytics() throws Exception {
            // Given
            IncidentAnalytics analytics = new IncidentAnalytics(
                    10L, 3L, 5L, 2L, 12.5,
                    Map.of("HIGH", 5L, "LOW", 5L),
                    Map.of("OPEN", 3L, "RESOLVED", 5L, "CLOSED", 2L),
                    Map.of("2026-01-10", 3L),
                    Map.of("2026-01-10", 2L),
                    5L, 3L, 8L, 6L);

            when(analyticsService.generateAnalytics(tenantId)).thenReturn(analytics);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalIncidents").value(10))
                    .andExpect(jsonPath("$.openIncidents").value(3))
                    .andExpect(jsonPath("$.resolvedIncidents").value(5))
                    .andExpect(jsonPath("$.averageResolutionTimeHours").value(12.5))
                    .andExpect(jsonPath("$.incidentsCreatedThisWeek").value(5));
        }

        @Test
        @DisplayName("should return empty analytics when no incidents")
        void shouldReturnEmptyAnalyticsWhenNoIncidents() throws Exception {
            // Given
            when(analyticsService.generateAnalytics(tenantId)).thenReturn(IncidentAnalytics.empty());

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalIncidents").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/incidents/analytics/severity-distribution")
    class GetSeverityDistribution {

        @Test
        @DisplayName("should return severity distribution")
        void shouldReturnSeverityDistribution() throws Exception {
            // Given
            Map<String, Long> distribution = Map.of("HIGH", 5L, "LOW", 3L, "CRITICAL", 2L);
            when(analyticsService.getSeverityDistribution(tenantId)).thenReturn(distribution);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics/severity-distribution", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.HIGH").value(5))
                    .andExpect(jsonPath("$.LOW").value(3))
                    .andExpect(jsonPath("$.CRITICAL").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/incidents/analytics/mttr")
    class GetMTTR {

        @Test
        @DisplayName("should return MTTR")
        void shouldReturnMTTR() throws Exception {
            // Given
            when(analyticsService.getMeanTimeToResolution(tenantId)).thenReturn(8.5);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics/mttr", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mttrHours").value(8.5));
        }

        @Test
        @DisplayName("should return N/A when no resolved incidents")
        void shouldReturnNAWhenNoResolvedIncidents() throws Exception {
            // Given
            when(analyticsService.getMeanTimeToResolution(tenantId)).thenReturn(null);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics/mttr", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mttrHours").value("N/A"));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/incidents/analytics/open-rate")
    class GetOpenRate {

        @Test
        @DisplayName("should return open rate")
        void shouldReturnOpenRate() throws Exception {
            // Given
            when(analyticsService.getOpenIncidentRate(tenantId)).thenReturn(25.5);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics/open-rate", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.openRatePercent").value(25.5));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/incidents/analytics/by-creator")
    class GetByCreator {

        @Test
        @DisplayName("should return incidents by creator")
        void shouldReturnIncidentsByCreator() throws Exception {
            // Given
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            Map<UUID, Long> byCreator = Map.of(user1, 5L, user2, 3L);
            when(analyticsService.getIncidentsByCreator(tenantId)).thenReturn(byCreator);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/analytics/by-creator", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + user1).value(5))
                    .andExpect(jsonPath("$." + user2).value(3));
        }
    }
}
