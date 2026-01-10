package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.IncidentAssignmentRepository;
import com.example.incidentplatform.domain.model.IncidentAssignment;
import com.example.incidentplatform.infrastructure.persistence.mapper.IncidentAssignmentMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.IncidentAssignmentJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class JpaIncidentAssignmentRepositoryAdapter implements IncidentAssignmentRepository {

    private final IncidentAssignmentJpaRepository jpaRepository;
    private final IncidentAssignmentMapper mapper;

    public JpaIncidentAssignmentRepositoryAdapter(
            IncidentAssignmentJpaRepository jpaRepository,
            IncidentAssignmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public IncidentAssignment save(IncidentAssignment assignment) {
        var entity = mapper.toEntity(assignment);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IncidentAssignment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentAssignment> findByIncidentId(UUID incidentId) {
        return jpaRepository.findByIncidentIdOrderByAssignedAtDesc(incidentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentAssignment> findActiveByIncidentId(UUID incidentId) {
        return jpaRepository.findActiveByIncidentId(incidentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentAssignment> findActiveByAssigneeId(UUID assigneeId) {
        return jpaRepository.findActiveByAssigneeId(assigneeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IncidentAssignment> findActiveAssignment(UUID incidentId, UUID assigneeId) {
        return jpaRepository.findActiveAssignment(incidentId, assigneeId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAssigned(UUID incidentId, UUID assigneeId) {
        return jpaRepository.isUserAssigned(incidentId, assigneeId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByIncidentId(UUID incidentId) {
        return jpaRepository.countActiveByIncidentId(incidentId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByAssigneeId(UUID assigneeId) {
        return jpaRepository.countActiveByAssigneeId(assigneeId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
