package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentAssignmentRepository;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.application.port.UserRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentAssignment;
import com.example.incidentplatform.domain.model.user.User;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class IncidentAssignmentService {

    private final IncidentAssignmentRepository assignmentRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final WebhookService webhookService;

    public IncidentAssignmentService(
            IncidentAssignmentRepository assignmentRepository,
            IncidentRepository incidentRepository,
            UserRepository userRepository,
            WebhookService webhookService) {
        this.assignmentRepository = assignmentRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.webhookService = webhookService;
    }

    public IncidentAssignment assignUser(UUID incidentId, UUID assigneeId, UUID assignedBy, String notes) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException("Incident not found with id: " + incidentId));

        if (assignmentRepository.isUserAssigned(incidentId, assigneeId)) {
            throw new ConflictException("User is already assigned to this incident");
        }

        IncidentAssignment assignment = IncidentAssignment.createNew(incidentId, assigneeId, assignedBy, notes);
        IncidentAssignment saved = assignmentRepository.save(assignment);

        // Trigger webhook for assignment
        triggerAssignmentWebhook(incident, assigneeId, assignedBy, WebhookEventType.INCIDENT_ASSIGNED);

        return saved;
    }

    public IncidentAssignment unassignUser(UUID incidentId, UUID assigneeId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException("Incident not found with id: " + incidentId));

        IncidentAssignment assignment = assignmentRepository.findActiveAssignment(incidentId, assigneeId)
                .orElseThrow(() -> new NotFoundException("Active assignment not found"));

        IncidentAssignment unassigned = assignment.unassign();
        IncidentAssignment saved = assignmentRepository.save(unassigned);

        // Trigger webhook for unassignment
        triggerAssignmentWebhook(incident, assigneeId, null, WebhookEventType.INCIDENT_UNASSIGNED);

        return saved;
    }

    private void triggerAssignmentWebhook(Incident incident, UUID assigneeId, UUID assignedBy,
            WebhookEventType eventType) {
        User assignee = userRepository.findById(assigneeId).orElse(null);
        User assigner = assignedBy != null ? userRepository.findById(assignedBy).orElse(null) : null;

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", incident.id().toString());
        eventData.put("title", incident.title());
        eventData.put("severity", incident.severity().name());
        eventData.put("status", incident.status().name());
        eventData.put("description", incident.description());
        eventData.put("assigneeId", assigneeId.toString());
        eventData.put("assigneeName", assignee != null ? assignee.displayName() : "Unknown User");
        eventData.put("assigneeEmail", assignee != null ? assignee.email() : "");
        if (assigner != null) {
            eventData.put("assignedByName", assigner.displayName());
        }

        webhookService.triggerWebhooks(incident.tenantId(), eventType, eventData);
    }

    public IncidentAssignment unassignById(UUID assignmentId) {
        IncidentAssignment assignment = getAssignment(assignmentId);

        if (!assignment.isActive()) {
            throw new ConflictException("Assignment is already inactive");
        }

        IncidentAssignment unassigned = assignment.unassign();
        return assignmentRepository.save(unassigned);
    }

    @Transactional(readOnly = true)
    public IncidentAssignment getAssignment(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found with id: " + assignmentId));
    }

    @Transactional(readOnly = true)
    public List<IncidentAssignment> getActiveAssignmentsForIncident(UUID incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new NotFoundException("Incident not found with id: " + incidentId);
        }
        return assignmentRepository.findActiveByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public List<IncidentAssignment> getAllAssignmentsForIncident(UUID incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new NotFoundException("Incident not found with id: " + incidentId);
        }
        return assignmentRepository.findByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public List<IncidentAssignment> getActiveAssignmentsForUser(UUID userId) {
        return assignmentRepository.findActiveByAssigneeId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isUserAssigned(UUID incidentId, UUID userId) {
        return assignmentRepository.isUserAssigned(incidentId, userId);
    }

    @Transactional(readOnly = true)
    public long countAssigneesForIncident(UUID incidentId) {
        return assignmentRepository.countActiveByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public long countIncidentsForUser(UUID userId) {
        return assignmentRepository.countActiveByAssigneeId(userId);
    }

    public IncidentAssignment updateNotes(UUID assignmentId, String notes) {
        IncidentAssignment assignment = getAssignment(assignmentId);
        IncidentAssignment updated = assignment.withNotes(notes);
        return assignmentRepository.save(updated);
    }
}
