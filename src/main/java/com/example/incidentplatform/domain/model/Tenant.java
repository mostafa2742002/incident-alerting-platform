package com.example.incidentplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record Tenant(
        UUID id,
        String slug,
        String name,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,64}$");

    public Tenant {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(slug, "slug must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (slug.isBlank() || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new IllegalArgumentException("slug must match " + SLUG_PATTERN.pattern());
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    public static Tenant createNew(String slug, String name) {
        Instant now = Instant.now();
        return new Tenant(
                UUID.randomUUID(),
                slug,
                name,
                TenantStatus.ACTIVE,
                now,
                now
        );
    }
}
