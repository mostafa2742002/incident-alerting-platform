package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.ManageIncidentUseCase;
import com.example.incidentplatform.api.dto.incident.CreateIncidentRequest;
import com.example.incidentplatform.api.dto.incident.IncidentResponse;
import com.example.incidentplatform.api.dto.incident.UpdateIncidentRequest;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;
import com.example.incidentplatform.infrastructure.security.SecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IncidentControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private ManageIncidentUseCase manageIncidentUseCase;

        @Mock
        private SecurityContextHelper securityContextHelper;

        @InjectMocks
        private IncidentController incidentController;

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(incidentController).build();
        }

        @Test
        @DisplayName("POST /api/public/tenants/{tenantId}/incidents creates new incident")
        void createIncident_returnsCreatedStatus() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                var request = new CreateIncidentRequest("Database Failed", "Connection pool exhausted",
                                Severity.CRITICAL);

                var incident = Incident.createNew(tenantId, request.title(), request.description(), request.severity(),
                                userId);

                when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.of(userId));
                when(manageIncidentUseCase.createIncident(tenantId, request.title(), request.description(),
                                request.severity(),
                                userId))
                                .thenReturn(incident);

                mockMvc.perform(post("/api/public/tenants/{tenantId}/incidents", tenantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString(request.title())))
                                .andExpect(content().string(
                                                org.hamcrest.Matchers.containsString(Severity.CRITICAL.name())));

                verify(manageIncidentUseCase).createIncident(tenantId, request.title(), request.description(),
                                request.severity(), userId);
        }

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents/{incidentId} retrieves incident")
        void getIncident_returnsIncident() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var incident = Incident.createNew(tenantId, "Issue", "Description", Severity.HIGH, userId);

                when(manageIncidentUseCase.getIncident(tenantId, incidentId))
                                .thenReturn(incident);

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/{incidentId}", tenantId, incidentId))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Issue")))
                                .andExpect(content()
                                                .string(org.hamcrest.Matchers.containsString(Severity.HIGH.name())));

                verify(manageIncidentUseCase).getIncident(tenantId, incidentId);
        }

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents lists all incidents")
        void listIncidents_returnsIncidentList() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var incident1 = Incident.createNew(tenantId, "Issue 1", "Desc", Severity.HIGH, userId);
                var incident2 = Incident.createNew(tenantId, "Issue 2", "Desc", Severity.LOW, userId);

                when(manageIncidentUseCase.listIncidents(tenantId))
                                .thenReturn(List.of(incident1, incident2));

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Issue 1")))
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Issue 2")));

                verify(manageIncidentUseCase).listIncidents(tenantId);
        }

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents/status/{status} filters by status")
        void listIncidentsByStatus_returnsFilteredList() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var incident = Incident.createNew(tenantId, "Open Issue", "Desc", Severity.MEDIUM, userId);

                when(manageIncidentUseCase.listIncidentsByStatus(tenantId, IncidentStatus.OPEN))
                                .thenReturn(List.of(incident));

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/status/{status}", tenantId, "OPEN"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Open Issue")));

                verify(manageIncidentUseCase).listIncidentsByStatus(tenantId, IncidentStatus.OPEN);
        }

        @Test
        @DisplayName("DELETE /api/public/tenants/{tenantId}/incidents/{incidentId} deletes incident")
        void deleteIncident_returnsNoContent() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();

                mockMvc.perform(delete("/api/public/tenants/{tenantId}/incidents/{incidentId}", tenantId, incidentId))
                                .andExpect(status().isNoContent());

                verify(manageIncidentUseCase).deleteIncident(tenantId, incidentId);
        }

        // ==================== Update Incident Tests ====================

        @Test
        @DisplayName("PATCH /api/public/tenants/{tenantId}/incidents/{incidentId} updates status")
        void updateIncident_updatesStatus() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var updateRequest = new UpdateIncidentRequest(null, null, null, "IN_PROGRESS");

                // Create an updated incident (simulating status change)
                var updated = Incident.of(
                                incidentId, tenantId, "Issue", "Desc",
                                Severity.HIGH, IncidentStatus.IN_PROGRESS,
                                userId, java.time.Instant.now(), java.time.Instant.now(), null);

                when(manageIncidentUseCase.updateIncident(
                                eq(tenantId), eq(incidentId), isNull(), isNull(), isNull(),
                                eq(IncidentStatus.IN_PROGRESS)))
                                .thenReturn(updated);

                mockMvc.perform(patch("/api/public/tenants/{tenantId}/incidents/{incidentId}", tenantId, incidentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("IN_PROGRESS")));

                verify(manageIncidentUseCase).updateIncident(
                                eq(tenantId), eq(incidentId), isNull(), isNull(), isNull(),
                                eq(IncidentStatus.IN_PROGRESS));
        }

        @Test
        @DisplayName("PATCH with multiple fields updates all provided fields")
        void updateIncident_updatesMultipleFields() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var updateRequest = new UpdateIncidentRequest("New Title", "New Description", "CRITICAL", "RESOLVED");

                var updated = Incident.of(
                                incidentId, tenantId, "New Title", "New Description",
                                Severity.CRITICAL, IncidentStatus.RESOLVED,
                                userId, java.time.Instant.now(), java.time.Instant.now(), java.time.Instant.now());

                when(manageIncidentUseCase.updateIncident(
                                eq(tenantId), eq(incidentId), eq("New Title"), eq("New Description"),
                                eq(Severity.CRITICAL), eq(IncidentStatus.RESOLVED)))
                                .thenReturn(updated);

                mockMvc.perform(patch("/api/public/tenants/{tenantId}/incidents/{incidentId}", tenantId, incidentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("New Title")))
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("CRITICAL")));
        }

        @Test
        @DisplayName("PATCH with empty body returns bad request")
        void updateIncident_emptyBody_returnsBadRequest() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();

                // All fields null = no updates
                var emptyRequest = new UpdateIncidentRequest(null, null, null, null);

                mockMvc.perform(patch("/api/public/tenants/{tenantId}/incidents/{incidentId}", tenantId, incidentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(emptyRequest)))
                                .andExpect(status().isBadRequest());

                verify(manageIncidentUseCase, never()).updateIncident(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("POST /api/public/tenants/{tenantId}/incidents/{incidentId}/escalate escalates severity")
        void escalateIncident_increasesSeverity() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                // MEDIUM → HIGH after escalation
                var escalated = Incident.of(
                                incidentId, tenantId, "Issue", "Desc",
                                Severity.HIGH, IncidentStatus.OPEN,
                                userId, java.time.Instant.now(), java.time.Instant.now(), null);

                when(manageIncidentUseCase.escalateIncident(tenantId, incidentId))
                                .thenReturn(escalated);

                mockMvc.perform(post("/api/public/tenants/{tenantId}/incidents/{incidentId}/escalate", tenantId,
                                incidentId))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("HIGH")));

                verify(manageIncidentUseCase).escalateIncident(tenantId, incidentId);
        }

        // ==================== Search & Filter Tests ====================

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents/search returns filtered results")
        void searchIncidents_returnsFilteredResults() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var incident = Incident.createNew(tenantId, "Server Down", "Database crash", Severity.HIGH, userId);

                when(manageIncidentUseCase.searchIncidents(eq(tenantId), any()))
                                .thenReturn(List.of(incident));

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/search", tenantId)
                                .param("q", "server")
                                .param("status", "OPEN")
                                .param("severity", "HIGH"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Server Down")));

                verify(manageIncidentUseCase).searchIncidents(eq(tenantId), any());
        }

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents/search with no params returns all")
        void searchIncidents_withNoParams_returnsAll() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var incident1 = Incident.createNew(tenantId, "Issue 1", "Desc", Severity.HIGH, userId);
                var incident2 = Incident.createNew(tenantId, "Issue 2", "Desc", Severity.LOW, userId);

                when(manageIncidentUseCase.searchIncidents(eq(tenantId), any()))
                                .thenReturn(List.of(incident1, incident2));

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/search", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Issue 1")))
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Issue 2")));
        }

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents/count returns status counts")
        void countIncidents_returnsStatusCounts() throws Exception {
                UUID tenantId = UUID.randomUUID();

                when(manageIncidentUseCase.countIncidents(tenantId)).thenReturn(10L);
                when(manageIncidentUseCase.countIncidentsByStatus(tenantId, IncidentStatus.OPEN)).thenReturn(5L);
                when(manageIncidentUseCase.countIncidentsByStatus(tenantId, IncidentStatus.IN_PROGRESS)).thenReturn(3L);
                when(manageIncidentUseCase.countIncidentsByStatus(tenantId, IncidentStatus.RESOLVED)).thenReturn(1L);
                when(manageIncidentUseCase.countIncidentsByStatus(tenantId, IncidentStatus.CLOSED)).thenReturn(1L);

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/count", tenantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.total").value(10))
                                .andExpect(jsonPath("$.open").value(5))
                                .andExpect(jsonPath("$.inProgress").value(3))
                                .andExpect(jsonPath("$.resolved").value(1))
                                .andExpect(jsonPath("$.closed").value(1));
        }

        @Test
        @DisplayName("GET /api/public/tenants/{tenantId}/incidents/severity/{severity} filters by severity")
        void listIncidentsBySeverity_returnsFilteredList() throws Exception {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();

                var incident = Incident.createNew(tenantId, "Critical Issue", "Desc", Severity.CRITICAL, userId);

                when(manageIncidentUseCase.listIncidentsBySeverity(tenantId, Severity.CRITICAL))
                                .thenReturn(List.of(incident));

                mockMvc.perform(get("/api/public/tenants/{tenantId}/incidents/severity/{severity}", tenantId,
                                "CRITICAL"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(org.hamcrest.Matchers.containsString("Critical Issue")));

                verify(manageIncidentUseCase).listIncidentsBySeverity(tenantId, Severity.CRITICAL);
        }
}
