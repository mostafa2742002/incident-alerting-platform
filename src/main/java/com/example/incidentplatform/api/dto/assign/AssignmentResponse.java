package com.example.incidentplatform.api.dto.assign;

import java.time.Instant;
import java.util.UUID;

import com.example.incidentplatform.domain.model.incident.IncidentAssignment;


public record AssignmentResponse(
        UUID id,
        UUID incidentId,
        UUID assigneeId,
        UUID assignedBy,
        Instant assignedAt,
        Instant unassignedAt,
        String notes,
        boolean active) {

    public static AssignmentResponse from(IncidentAssignment assignment) {
        return new AssignmentResponse(
                assignment.id(),
                assignment.incidentId(),
                assignment.assigneeId(),
                assignment.assignedBy(),
                assignment.assignedAt(),
                assignment.unassignedAt(),
                assignment.notes(),
                assignment.isActive());
    }
}
