package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.ManageIncidentUseCase;
import com.example.incidentplatform.api.dto.CreateIncidentRequest;
import com.example.incidentplatform.api.dto.IncidentResponse;
import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
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
        var request = new CreateIncidentRequest("Database Failed", "Connection pool exhausted", Severity.CRITICAL);

        var incident = Incident.createNew(tenantId, request.title(), request.description(), request.severity(), userId);

        when(securityContextHelper.getCurrentUserId()).thenReturn(Optional.of(userId));
        when(manageIncidentUseCase.createIncident(tenantId, request.title(), request.description(), request.severity(),
                userId))
                .thenReturn(incident);

        mockMvc.perform(post("/api/public/tenants/{tenantId}/incidents", tenantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(request.title())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(Severity.CRITICAL.name())));

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
                .andExpect(content().string(org.hamcrest.Matchers.containsString(Severity.HIGH.name())));

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
}
