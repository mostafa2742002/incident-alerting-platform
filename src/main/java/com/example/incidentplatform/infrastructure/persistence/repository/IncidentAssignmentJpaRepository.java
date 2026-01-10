package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.IncidentAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentAssignmentJpaRepository extends JpaRepository<IncidentAssignmentEntity, UUID> {

    List<IncidentAssignmentEntity> findByIncidentIdOrderByAssignedAtDesc(UUID incidentId);

    @Query("SELECT a FROM IncidentAssignmentEntity a WHERE a.incidentId = :incidentId AND a.unassignedAt IS NULL ORDER BY a.assignedAt DESC")
    List<IncidentAssignmentEntity> findActiveByIncidentId(@Param("incidentId") UUID incidentId);

    @Query("SELECT a FROM IncidentAssignmentEntity a WHERE a.assigneeId = :assigneeId AND a.unassignedAt IS NULL ORDER BY a.assignedAt DESC")
    List<IncidentAssignmentEntity> findActiveByAssigneeId(@Param("assigneeId") UUID assigneeId);

    @Query("SELECT a FROM IncidentAssignmentEntity a WHERE a.incidentId = :incidentId AND a.assigneeId = :assigneeId AND a.unassignedAt IS NULL")
    Optional<IncidentAssignmentEntity> findActiveAssignment(
            @Param("incidentId") UUID incidentId,
            @Param("assigneeId") UUID assigneeId);

    @Query("SELECT COUNT(a) > 0 FROM IncidentAssignmentEntity a WHERE a.incidentId = :incidentId AND a.assigneeId = :assigneeId AND a.unassignedAt IS NULL")
    boolean isUserAssigned(@Param("incidentId") UUID incidentId, @Param("assigneeId") UUID assigneeId);

    @Query("SELECT COUNT(a) FROM IncidentAssignmentEntity a WHERE a.incidentId = :incidentId AND a.unassignedAt IS NULL")
    long countActiveByIncidentId(@Param("incidentId") UUID incidentId);

    @Query("SELECT COUNT(a) FROM IncidentAssignmentEntity a WHERE a.assigneeId = :assigneeId AND a.unassignedAt IS NULL")
    long countActiveByAssigneeId(@Param("assigneeId") UUID assigneeId);
}
