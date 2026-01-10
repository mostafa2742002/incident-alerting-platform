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

    /**
     * Update an existing incident with new values.
     * 
     * @param tenantId    The tenant that owns the incident
     * @param incidentId  The incident to update
     * @param title       New title (null to keep current)
     * @param description New description (null to keep current)
     * @param severity    New severity (null to keep current)
     * @param status      New status (null to keep current)
     * @return The updated incident
     * @throws NotFoundException if incident doesn't exist in tenant
     */
    public Incident updateIncident(
            UUID tenantId,
            UUID incidentId,
            String title,
            String description,
            Severity severity,
            IncidentStatus status) {

        // 1. Find existing incident (validates tenant ownership)
        Incident existing = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));

        // 2. Apply updates using domain method
        Incident updated = existing.update(title, description, severity, status);

        // 3. Save and return
        return incidentRepository.save(updated);
    }

    /**
     * Quick status transition (convenience method).
     */
    public Incident changeStatus(UUID tenantId, UUID incidentId, IncidentStatus newStatus) {
        return updateIncident(tenantId, incidentId, null, null, null, newStatus);
    }

    /**
     * Escalate incident severity by one level.
     */
    public Incident escalateIncident(UUID tenantId, UUID incidentId) {
        Incident existing = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));

        Incident escalated = existing.escalate();
        return incidentRepository.save(escalated);
    }
}
