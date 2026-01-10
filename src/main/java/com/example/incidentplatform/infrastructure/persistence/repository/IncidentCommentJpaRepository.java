package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.IncidentCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentCommentJpaRepository extends JpaRepository<IncidentCommentEntity, UUID> {

    List<IncidentCommentEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);

    List<IncidentCommentEntity> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);

    List<IncidentCommentEntity> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    long countByIncidentId(UUID incidentId);

    void deleteByIncidentId(UUID incidentId);
}
