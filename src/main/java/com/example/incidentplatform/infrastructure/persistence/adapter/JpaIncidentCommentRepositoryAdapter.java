package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.IncidentCommentRepository;
import com.example.incidentplatform.domain.model.IncidentComment;
import com.example.incidentplatform.infrastructure.persistence.mapper.IncidentCommentMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.IncidentCommentJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class JpaIncidentCommentRepositoryAdapter implements IncidentCommentRepository {

    private final IncidentCommentJpaRepository jpaRepository;
    private final IncidentCommentMapper mapper;

    public JpaIncidentCommentRepositoryAdapter(IncidentCommentJpaRepository jpaRepository,
            IncidentCommentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public IncidentComment save(IncidentComment comment) {
        var entity = mapper.toEntity(comment);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IncidentComment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentComment> findByIncidentId(UUID incidentId) {
        return jpaRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentComment> findByIncidentIdNewestFirst(UUID incidentId) {
        return jpaRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentComment> findByAuthorId(UUID authorId) {
        return jpaRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByIncidentId(UUID incidentId) {
        return jpaRepository.countByIncidentId(incidentId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByIncidentId(UUID incidentId) {
        jpaRepository.deleteByIncidentId(incidentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
