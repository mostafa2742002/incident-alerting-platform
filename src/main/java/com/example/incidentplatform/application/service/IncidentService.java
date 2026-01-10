package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.api.dto.incident.IncidentSearchCriteria;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;

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

    public List<Incident> listIncidentsBySeverity(UUID tenantId, Severity severity) {
        return incidentRepository.findByTenantIdAndSeverity(tenantId, severity.name());
    }

    public List<Incident> searchIncidents(UUID tenantId, IncidentSearchCriteria criteria) {
        return incidentRepository.search(
                tenantId,
                criteria.searchTerm(),
                criteria.status() != null ? criteria.status().name() : null,
                criteria.severity() != null ? criteria.severity().name() : null,
                criteria.createdAfter(),
                criteria.createdBefore(),
                criteria.resolved(),
                criteria.sortBy(),
                criteria.sortDirection());
    }

    public long countIncidents(UUID tenantId) {
        return incidentRepository.countByTenantId(tenantId);
    }

    public long countIncidentsByStatus(UUID tenantId, IncidentStatus status) {
        return incidentRepository.countByTenantIdAndStatus(tenantId, status.name());
    }

    public void deleteIncident(UUID tenantId, UUID incidentId) {
        if (!incidentRepository.existsByIdAndTenantId(incidentId, tenantId)) {
            throw new NotFoundException("Incident not found: " + incidentId);
        }
        incidentRepository.deleteByIdAndTenantId(incidentId, tenantId);
    }

    public Incident updateIncident(
            UUID tenantId,
            UUID incidentId,
            String title,
            String description,
            Severity severity,
            IncidentStatus status) {

        Incident existing = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));

        Incident updated = existing.update(title, description, severity, status);

        return incidentRepository.save(updated);
    }

    public Incident changeStatus(UUID tenantId, UUID incidentId, IncidentStatus newStatus) {
        return updateIncident(tenantId, incidentId, null, null, null, newStatus);
    }

    public Incident escalateIncident(UUID tenantId, UUID incidentId) {
        Incident existing = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));

        Incident escalated = existing.escalate();
        return incidentRepository.save(escalated);
    }
}
