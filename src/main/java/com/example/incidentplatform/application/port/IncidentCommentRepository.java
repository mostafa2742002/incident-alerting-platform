package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.IncidentComment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface IncidentCommentRepository {

    IncidentComment save(IncidentComment comment);

    Optional<IncidentComment> findById(UUID id);

    List<IncidentComment> findByIncidentId(UUID incidentId);

    List<IncidentComment> findByIncidentIdNewestFirst(UUID incidentId);

    List<IncidentComment> findByAuthorId(UUID authorId);

    long countByIncidentId(UUID incidentId);

    void deleteById(UUID id);

    void deleteByIncidentId(UUID incidentId);

    boolean existsById(UUID id);
}
