package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.domain.model.IncidentStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public Incident createIncident(UUID tenantId, String title, String description, Severity severity, UUID createdBy) {
        var incident = Incident.createNew(tenantId, title, description, severity, createdBy);
        return incidentRepository.save(incident);
    }

    public Incident getIncident(UUID tenantId, UUID incidentId) {
        return incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));
    }

    public List<Incident> listIncidents(UUID tenantId) {
        return incidentRepository.findByTenantId(tenantId);
    }

    public List<Incident> listIncidentsByStatus(UUID tenantId, IncidentStatus status) {
        return incidentRepository.findByTenantIdAndStatus(tenantId, status.name());
    }

    public void deleteIncident(UUID tenantId, UUID incidentId) {
        if (!incidentRepository.existsByIdAndTenantId(incidentId, tenantId)) {
            throw new NotFoundException("Incident not found: " + incidentId);
        }
        incidentRepository.deleteByIdAndTenantId(incidentId, tenantId);
    }
}
