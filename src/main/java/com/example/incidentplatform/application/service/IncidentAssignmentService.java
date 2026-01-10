package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentAssignmentRepository;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.incident.IncidentAssignment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IncidentAssignmentService {

    private final IncidentAssignmentRepository assignmentRepository;
    private final IncidentRepository incidentRepository;

    public IncidentAssignmentService(
            IncidentAssignmentRepository assignmentRepository,
            IncidentRepository incidentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.incidentRepository = incidentRepository;
    }

    public IncidentAssignment assignUser(UUID incidentId, UUID assigneeId, UUID assignedBy, String notes) {
       
        if (!incidentRepository.existsById(incidentId)) {
            throw new NotFoundException("Incident not found with id: " + incidentId);
        }

        if (assignmentRepository.isUserAssigned(incidentId, assigneeId)) {
            throw new ConflictException("User is already assigned to this incident");
        }

        IncidentAssignment assignment = IncidentAssignment.createNew(incidentId, assigneeId, assignedBy, notes);
        return assignmentRepository.save(assignment);
    }

    public IncidentAssignment unassignUser(UUID incidentId, UUID assigneeId) {
        IncidentAssignment assignment = assignmentRepository.findActiveAssignment(incidentId, assigneeId)
                .orElseThrow(() -> new NotFoundException("Active assignment not found"));

        IncidentAssignment unassigned = assignment.unassign();
        return assignmentRepository.save(unassigned);
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
