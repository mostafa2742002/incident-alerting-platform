package com.example.incidentplatform.domain.model.incident;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

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

    // ==================== Update Methods ====================
    // These methods return NEW Incident instances (immutability pattern)
    // This ensures domain objects are predictable and thread-safe

    public Incident update(String newTitle, String newDescription, Severity newSeverity, IncidentStatus newStatus) {
        Instant now = Instant.now();
        IncidentStatus finalStatus = newStatus != null ? newStatus : this.status;

        // Set resolvedAt when incident becomes resolved
        Instant finalResolvedAt = this.resolvedAt;
        if (newStatus == IncidentStatus.RESOLVED && this.resolvedAt == null) {
            finalResolvedAt = now;
        }

        return new Incident(
                this.id,
                this.tenantId,
                newTitle != null ? newTitle : this.title,
                newDescription != null ? newDescription : this.description,
                newSeverity != null ? newSeverity : this.severity,
                finalStatus,
                this.createdBy,
                this.createdAt,
                now, // updatedAt always updates
                finalResolvedAt);
    }

    public Incident withStatus(IncidentStatus newStatus) {
        return update(null, null, null, newStatus);
    }

    public Incident withSeverity(Severity newSeverity) {
        return update(null, null, newSeverity, null);
    }

    public Incident escalate() {
        Severity escalated = this.severity.escalate();
        if (escalated == this.severity) {
            return this; // Already at max severity
        }
        return withSeverity(escalated);
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
