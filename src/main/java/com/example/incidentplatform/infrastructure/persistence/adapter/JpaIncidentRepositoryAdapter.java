package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.domain.model.Incident;
import com.example.incidentplatform.infrastructure.persistence.mapper.IncidentMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.IncidentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaIncidentRepositoryAdapter implements IncidentRepository {

    private final IncidentJpaRepository jpaRepository;
    private final IncidentMapper mapper;

    public JpaIncidentRepositoryAdapter(IncidentJpaRepository jpaRepository, IncidentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Incident save(Incident incident) {
        var entity = mapper.toEntity(incident);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Incident> findByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.findById(id)
                .filter(entity -> entity.getTenantId().equals(tenantId))
                .map(mapper::toDomain);
    }

    @Override
    public List<Incident> findByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findByTenantIdAndStatus(UUID tenantId, String status) {
        return jpaRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByIdAndTenantId(UUID id, UUID tenantId) {
        if (existsByIdAndTenantId(id, tenantId)) {
            jpaRepository.deleteById(id);
        }
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return jpaRepository.existsByIdAndTenantId(id, tenantId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
