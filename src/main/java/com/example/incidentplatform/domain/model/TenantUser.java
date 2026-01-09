package com.example.incidentplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TenantUser(
        UUID id,
        UUID tenantId,
        UUID userId,
        RoleCode roleCode,
        Instant createdAt) {

    public TenantUser {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleCode, "roleCode must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static TenantUser createNew(UUID tenantId, UUID userId, RoleCode roleCode) {
        return new TenantUser(
                UUID.randomUUID(),
                tenantId,
                userId,
                roleCode,
                Instant.now());
    }

    public static TenantUser of(UUID id, UUID tenantId, UUID userId, RoleCode roleCode, Instant createdAt) {
        return new TenantUser(id, tenantId, userId, roleCode, createdAt);
    }
}
