package com.example.incidentplatform.infrastructure.persistence.mapper;

import com.example.incidentplatform.domain.model.RefreshToken;
import com.example.incidentplatform.infrastructure.persistence.entity.RefreshTokenEntity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class RefreshTokenMapper {

    private RefreshTokenMapper() {}

    public static RefreshToken toDomain(RefreshTokenEntity e) {
        return new RefreshToken(
                e.getId(),
                e.getUserId(),
                e.getTokenHash(),
                toInstant(e.getExpiresAt()),
                toInstantOrNull(e.getRevokedAt()),
                toInstant(e.getCreatedAt())
        );
    }

    public static RefreshTokenEntity toEntity(RefreshToken t) {
        return new RefreshTokenEntity(
                t.id(),
                t.userId(),
                t.tokenHash(),
                toOffsetDateTimeOrNull(t.revokedAt()),
                toOffsetDateTime(t.expiresAt()),
                toOffsetDateTime(t.createdAt())
        );
    }

    private static Instant toInstant(OffsetDateTime odt) {
        return odt.toInstant();
    }

    private static Instant toInstantOrNull(OffsetDateTime odt) {
        return odt == null ? null : odt.toInstant();
    }

    private static OffsetDateTime toOffsetDateTime(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static OffsetDateTime toOffsetDateTimeOrNull(Instant instant) {
        return instant == null ? null : toOffsetDateTime(instant);
    }
}
