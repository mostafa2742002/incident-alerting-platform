package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.TenantRepository;
import com.example.incidentplatform.domain.model.Tenant;
import com.example.incidentplatform.infrastructure.persistence.mapper.TenantMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.TenantJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaTenantRepositoryAdapter implements TenantRepository {

    private final TenantJpaRepository tenantJpaRepository;

    public JpaTenantRepositoryAdapter(TenantJpaRepository tenantJpaRepository) {
        this.tenantJpaRepository = tenantJpaRepository;
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return tenantJpaRepository.findBySlug(slug).map(TenantMapper::toDomain);
    }

    @Override
    public Tenant save(Tenant tenant) {
        var saved = tenantJpaRepository.save(TenantMapper.toEntity(tenant));
        return TenantMapper.toDomain(saved);
    }
}
