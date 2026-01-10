package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.service.IncidentService;
import com.example.incidentplatform.api.dto.incident.IncidentSearchCriteria;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ManageIncidentUseCase {

    private final IncidentService incidentService;

    public ManageIncidentUseCase(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    public Incident createIncident(UUID tenantId, String title, String description, Severity severity, UUID createdBy) {
        return incidentService.createIncident(tenantId, title, description, severity, createdBy);
    }

    public Incident getIncident(UUID tenantId, UUID incidentId) {
        return incidentService.getIncident(tenantId, incidentId);
    }

    public List<Incident> listIncidents(UUID tenantId) {
        return incidentService.listIncidents(tenantId);
    }

    public List<Incident> listIncidentsByStatus(UUID tenantId, IncidentStatus status) {
        return incidentService.listIncidentsByStatus(tenantId, status);
    }

    public List<Incident> listIncidentsBySeverity(UUID tenantId, Severity severity) {
        return incidentService.listIncidentsBySeverity(tenantId, severity);
    }

    public List<Incident> searchIncidents(UUID tenantId, IncidentSearchCriteria criteria) {
        return incidentService.searchIncidents(tenantId, criteria);
    }

    public long countIncidents(UUID tenantId) {
        return incidentService.countIncidents(tenantId);
    }

    public long countIncidentsByStatus(UUID tenantId, IncidentStatus status) {
        return incidentService.countIncidentsByStatus(tenantId, status);
    }

    public void deleteIncident(UUID tenantId, UUID incidentId) {
        incidentService.deleteIncident(tenantId, incidentId);
    }

    public Incident updateIncident(
            UUID tenantId,
            UUID incidentId,
            String title,
            String description,
            Severity severity,
            IncidentStatus status) {
        return incidentService.updateIncident(tenantId, incidentId, title, description, severity, status);
    }

    public Incident escalateIncident(UUID tenantId, UUID incidentId) {
        return incidentService.escalateIncident(tenantId, incidentId);
    }
}
