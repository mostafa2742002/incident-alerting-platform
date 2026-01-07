package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.RefreshTokenRepository;
import com.example.incidentplatform.domain.model.RefreshToken;
import com.example.incidentplatform.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.RefreshTokenJpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpa;

    public JpaRefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(RefreshTokenMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        var saved = jpa.save(RefreshTokenMapper.toEntity(refreshToken));
        return RefreshTokenMapper.toDomain(saved);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        // For now: delete all refresh tokens for this user (simple “logout everywhere”).
        // Later we can change to “mark revoked_at” instead of delete if we want audit.
        jpa.deleteByUserId(userId);
    }
}
