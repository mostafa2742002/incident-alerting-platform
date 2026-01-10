package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.IncidentAssignment;
import com.example.incidentplatform.infrastructure.persistence.entity.IncidentAssignmentEntity;
import org.springframework.stereotype.Component;

@Component
public class IncidentAssignmentMapper {

    public IncidentAssignmentEntity toEntity(IncidentAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        return new IncidentAssignmentEntity(
                assignment.id(),
                assignment.incidentId(),
                assignment.assigneeId(),
                assignment.assignedBy(),
                assignment.assignedAt(),
                assignment.unassignedAt(),
                assignment.notes());
    }

    public IncidentAssignment toDomain(IncidentAssignmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return IncidentAssignment.of(
                entity.getId(),
                entity.getIncidentId(),
                entity.getAssigneeId(),
                entity.getAssignedBy(),
                entity.getAssignedAt(),
                entity.getUnassignedAt(),
                entity.getNotes());
    }
}
