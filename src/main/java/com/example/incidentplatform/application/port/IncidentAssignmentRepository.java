package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.IncidentAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface IncidentAssignmentRepository {

    IncidentAssignment save(IncidentAssignment assignment);

    Optional<IncidentAssignment> findById(UUID id);

    List<IncidentAssignment> findByIncidentId(UUID incidentId);

    List<IncidentAssignment> findActiveByIncidentId(UUID incidentId);

    List<IncidentAssignment> findActiveByAssigneeId(UUID assigneeId);

    Optional<IncidentAssignment> findActiveAssignment(UUID incidentId, UUID assigneeId);

    boolean isUserAssigned(UUID incidentId, UUID assigneeId);

    long countActiveByIncidentId(UUID incidentId);

    long countActiveByAssigneeId(UUID assigneeId);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
