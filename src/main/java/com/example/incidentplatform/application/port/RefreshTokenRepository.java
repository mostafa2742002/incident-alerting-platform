package com.example.incidentplatform.application.port;

import java.util.Optional;
import java.util.UUID;

import com.example.incidentplatform.domain.model.refresh.RefreshToken;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    void revokeAllByUserId(UUID userId);

    void revokeByTokenHash(String tokenHash);

}
