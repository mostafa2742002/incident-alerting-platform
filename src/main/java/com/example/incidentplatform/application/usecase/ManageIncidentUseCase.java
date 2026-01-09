package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.service.IncidentService;
import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Use case for managing incidents.
 * Orchestrates incident operations at the application boundary.
 */
@Component
public class ManageIncidentUseCase {

    private final IncidentService incidentService;

    public ManageIncidentUseCase(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /**
     * Create a new incident.
     */
    public Incident createIncident(UUID tenantId, String title, String description, Severity severity, UUID createdBy) {
        return incidentService.createIncident(tenantId, title, description, severity, createdBy);
    }

    /**
     * Get an incident.
     */
    public Incident getIncident(UUID tenantId, UUID incidentId) {
        return incidentService.getIncident(tenantId, incidentId);
    }

    /**
     * List all incidents for a tenant.
     */
    public List<Incident> listIncidents(UUID tenantId) {
        return incidentService.listIncidents(tenantId);
    }

    /**
     * List incidents by status.
     */
    public List<Incident> listIncidentsByStatus(UUID tenantId, IncidentStatus status) {
        return incidentService.listIncidentsByStatus(tenantId, status);
    }

    /**
     * Delete an incident.
     */
    public void deleteIncident(UUID tenantId, UUID incidentId) {
        incidentService.deleteIncident(tenantId, incidentId);
    }
}
