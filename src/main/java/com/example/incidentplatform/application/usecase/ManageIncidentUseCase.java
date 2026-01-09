package com.example.incidentplatform.application.usecase;

import com.example.incidentplatform.application.service.IncidentService;
import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
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

    public void deleteIncident(UUID tenantId, UUID incidentId) {
        incidentService.deleteIncident(tenantId, incidentId);
    }
}
