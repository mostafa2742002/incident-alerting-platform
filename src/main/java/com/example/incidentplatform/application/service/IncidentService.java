package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.application.port.UserRepository;
import com.example.incidentplatform.api.dto.incident.IncidentSearchCriteria;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;
import com.example.incidentplatform.domain.model.user.User;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final WebhookService webhookService;
    private final UserRepository userRepository;

    public IncidentService(IncidentRepository incidentRepository,
            WebhookService webhookService,
            UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.webhookService = webhookService;
        this.userRepository = userRepository;
    }

    public Incident createIncident(UUID tenantId, String title, String description, Severity severity, UUID createdBy) {
        var incident = Incident.createNew(tenantId, title, description, severity, createdBy);
        Incident saved = incidentRepository.save(incident);

        // Trigger webhook for incident creation
        triggerIncidentWebhook(saved, WebhookEventType.INCIDENT_CREATED, null);

        return saved;
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

        IncidentStatus previousStatus = existing.status();
        Incident updated = existing.update(title, description, severity, status);
        Incident saved = incidentRepository.save(updated);

        // Determine the right webhook event type
        WebhookEventType eventType;
        if (status == IncidentStatus.RESOLVED && previousStatus != IncidentStatus.RESOLVED) {
            eventType = WebhookEventType.INCIDENT_RESOLVED;
        } else if (status == IncidentStatus.CLOSED && previousStatus != IncidentStatus.CLOSED) {
            eventType = WebhookEventType.INCIDENT_CLOSED;
        } else {
            eventType = WebhookEventType.INCIDENT_UPDATED;
        }

        // Trigger webhook with resolution time for resolved incidents
        String resolutionTime = null;
        if (eventType == WebhookEventType.INCIDENT_RESOLVED && saved.resolvedAt() != null) {
            Duration duration = Duration.between(saved.createdAt(), saved.resolvedAt());
            resolutionTime = formatDuration(duration);
        }

        triggerIncidentWebhook(saved, eventType, resolutionTime);

        return saved;
    }

    public Incident changeStatus(UUID tenantId, UUID incidentId, IncidentStatus newStatus) {
        return updateIncident(tenantId, incidentId, null, null, null, newStatus);
    }

    public Incident escalateIncident(UUID tenantId, UUID incidentId) {
        Incident existing = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("Incident not found: " + incidentId));

        Incident escalated = existing.escalate();
        Incident saved = incidentRepository.save(escalated);

        // Trigger webhook if severity actually changed
        if (!escalated.severity().equals(existing.severity())) {
            triggerIncidentWebhook(saved, WebhookEventType.INCIDENT_ESCALATED, null);
        }

        return saved;
    }

    // ==================== Webhook Helper Methods ====================

    private void triggerIncidentWebhook(Incident incident, WebhookEventType eventType, String resolutionTime) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", incident.id().toString());
        eventData.put("title", incident.title());
        eventData.put("severity", incident.severity().name());
        eventData.put("status", incident.status().name());
        eventData.put("description", incident.description());
        eventData.put("createdAt", incident.createdAt().toString());

        // Add creator info
        User creator = userRepository.findById(incident.createdBy()).orElse(null);
        if (creator != null) {
            eventData.put("createdByName", creator.displayName());
        }

        // Add resolution info for resolved events
        if (eventType == WebhookEventType.INCIDENT_RESOLVED) {
            if (incident.resolvedAt() != null) {
                eventData.put("resolvedAt", incident.resolvedAt().toString());
            }
            if (resolutionTime != null) {
                eventData.put("resolutionTime", resolutionTime);
            }
            // Note: For a full implementation, you'd track who resolved it
            // For now, we'll indicate it was resolved
            eventData.put("resolvedByName", creator != null ? creator.displayName() : "System");
        }

        webhookService.triggerWebhooks(incident.tenantId(), eventType, eventData);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm", minutes);
        } else {
            return String.format("%ds", duration.toSecondsPart());
        }
    }
}
