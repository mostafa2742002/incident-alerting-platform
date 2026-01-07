package com.example.incidentplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RefreshToken(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt,
        Instant createdAt
) {
    public RefreshToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tokenHash, "tokenHash must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");

        if (tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now) || expiresAt.equals(now);
    }

    public static RefreshToken createNew(UUID userId, String tokenHash, Instant expiresAt) {
        Instant now = Instant.now();
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                expiresAt,
                null,
                now
        );
    }

    public RefreshToken revokeNow() {
        return new RefreshToken(id, userId, tokenHash, expiresAt, Instant.now(), createdAt);
    }
}
