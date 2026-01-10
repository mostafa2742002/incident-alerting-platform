package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.TenantUserRepository;
import com.example.incidentplatform.domain.model.tenant.TenantUser;
import com.example.incidentplatform.infrastructure.persistence.mapper.TenantUserMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public class JpaTenantUserRepositoryAdapter implements TenantUserRepository {

    private final TenantUserJpaRepository tenantUserJpaRepository;

    public JpaTenantUserRepositoryAdapter(TenantUserJpaRepository tenantUserJpaRepository) {
        this.tenantUserJpaRepository = tenantUserJpaRepository;
    }

    @Override
    public TenantUser save(TenantUser tenantUser) {
        var entity = TenantUserMapper.toEntity(tenantUser);
        var saved = tenantUserJpaRepository.save(entity);
        return TenantUserMapper.toDomain(saved);
    }

    @Override
    public Optional<TenantUser> findById(UUID id) {
        return tenantUserJpaRepository.findById(id)
                .map(TenantUserMapper::toDomain);
    }

    @Override
    public Optional<TenantUser> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return tenantUserJpaRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(TenantUserMapper::toDomain);
    }

    @Override
    public List<TenantUser> findByTenantId(UUID tenantId) {
        return tenantUserJpaRepository.findByTenantId(tenantId).stream()
                .map(TenantUserMapper::toDomain)
                .toList();
    }

    @Override
    public List<TenantUser> findByUserId(UUID userId) {
        return tenantUserJpaRepository.findByUserId(userId).stream()
                .map(TenantUserMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return tenantUserJpaRepository.existsByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public void delete(TenantUser tenantUser) {
        var entity = TenantUserMapper.toEntity(tenantUser);
        tenantUserJpaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteByTenantIdAndUserId(UUID tenantId, UUID userId) {
        tenantUserJpaRepository.deleteByTenantIdAndUserId(tenantId, userId);
    }
}
