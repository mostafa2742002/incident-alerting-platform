package com.example.incidentplatform.domain.model;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

/**
 * Domain model for an Incident.
 * Represents an issue or event that needs to be tracked and resolved within a
 * tenant.
 */
public class Incident {
    private final UUID id;
    private final UUID tenantId;
    private final String title;
    private final String description;
    private final Severity severity;
    private final IncidentStatus status;
    private final UUID createdBy;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant resolvedAt;

    private Incident(
            UUID id,
            UUID tenantId,
            String title,
            String description,
            Severity severity,
            IncidentStatus status,
            UUID createdBy,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.severity = Objects.requireNonNull(severity, "severity cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
        this.resolvedAt = resolvedAt;
    }

    /**
     * Factory method to create a new incident.
     */
    public static Incident createNew(
            UUID tenantId,
            String title,
            String description,
            Severity severity,
            UUID createdBy) {
        Instant now = Instant.now();
        return new Incident(
                UUID.randomUUID(),
                tenantId,
                title,
                description,
                severity,
                IncidentStatus.OPEN,
                createdBy,
                now,
                now,
                null);
    }

    /**
     * Reconstruct incident from persistence (for repository use).
     */
    public static Incident of(
            UUID id,
            UUID tenantId,
            String title,
            String description,
            Severity severity,
            IncidentStatus status,
            UUID createdBy,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt) {
        return new Incident(id, tenantId, title, description, severity, status, createdBy, createdAt, updatedAt,
                resolvedAt);
    }

    // Getters
    public UUID id() {
        return id;
    }

    public UUID tenantId() {
        return tenantId;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Severity severity() {
        return severity;
    }

    public IncidentStatus status() {
        return status;
    }

    public UUID createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant resolvedAt() {
        return resolvedAt;
    }

    public boolean isOpen() {
        return status == IncidentStatus.OPEN;
    }

    public boolean isResolved() {
        return status == IncidentStatus.RESOLVED || status == IncidentStatus.CLOSED;
    }

    public boolean isCritical() {
        return severity.isCritical();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Incident incident = (Incident) o;
        return Objects.equals(id, incident.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", title='" + title + '\'' +
                ", severity=" + severity +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
