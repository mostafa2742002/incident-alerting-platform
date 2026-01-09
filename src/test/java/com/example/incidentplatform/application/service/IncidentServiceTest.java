package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IncidentService.
 */
@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    private IncidentService incidentService;

    @BeforeEach
    void setup() {
        incidentService = new IncidentService(incidentRepository);
    }

    @Test
    @DisplayName("createIncident saves and returns new incident")
    void createIncident_savesAndReturnsIncident() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String title = "Database Connection Failed";
        String description = "Connection pool exhausted";
        Severity severity = Severity.CRITICAL;

        var incident = Incident.createNew(tenantId, title, description, severity, userId);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

        var result = incidentService.createIncident(tenantId, title, description, severity, userId);

        assertNotNull(result);
        assertEquals(title, result.title());
        assertEquals(severity, result.severity());
        assertEquals(IncidentStatus.OPEN, result.status());
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    @DisplayName("getIncident retrieves incident by ID")
    void getIncident_retrievesIncidentById() {
        UUID tenantId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        var incident = Incident.createNew(tenantId, "Issue", "Description", Severity.HIGH, UUID.randomUUID());

        when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                .thenReturn(Optional.of(incident));

        var result = incidentService.getIncident(tenantId, incidentId);

        assertNotNull(result);
        assertEquals(incident.id(), result.id());
    }

    @Test
    @DisplayName("getIncident throws NotFoundException when incident not found")
    void getIncident_throwsNotFoundException_whenNotFound() {
        UUID tenantId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> incidentService.getIncident(tenantId, incidentId));
    }

    @Test
    @DisplayName("listIncidents returns all incidents for tenant")
    void listIncidents_returnsAllIncidentsForTenant() {
        UUID tenantId = UUID.randomUUID();
        var incident1 = Incident.createNew(tenantId, "Issue 1", "Desc", Severity.HIGH, UUID.randomUUID());
        var incident2 = Incident.createNew(tenantId, "Issue 2", "Desc", Severity.LOW, UUID.randomUUID());

        when(incidentRepository.findByTenantId(tenantId))
                .thenReturn(List.of(incident1, incident2));

        var result = incidentService.listIncidents(tenantId);

        assertEquals(2, result.size());
        verify(incidentRepository).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("deleteIncident removes incident successfully")
    void deleteIncident_removesIncidentSuccessfully() {
        UUID tenantId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        when(incidentRepository.existsByIdAndTenantId(incidentId, tenantId)).thenReturn(true);

        incidentService.deleteIncident(tenantId, incidentId);

        verify(incidentRepository).deleteByIdAndTenantId(incidentId, tenantId);
    }

    @Test
    @DisplayName("deleteIncident throws NotFoundException when incident not found")
    void deleteIncident_throwsNotFoundException_whenNotFound() {
        UUID tenantId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        when(incidentRepository.existsByIdAndTenantId(incidentId, tenantId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> incidentService.deleteIncident(tenantId, incidentId));

        verify(incidentRepository, never()).deleteByIdAndTenantId(any(), any());
    }
}
