package com.example.incidentplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class IncidentAssignment {

    private final UUID id;
    private final UUID incidentId;
    private final UUID assigneeId;
    private final UUID assignedBy;
    private final Instant assignedAt;
    private final Instant unassignedAt;
    private final String notes;

    private IncidentAssignment(
            UUID id,
            UUID incidentId,
            UUID assigneeId,
            UUID assignedBy,
            Instant assignedAt,
            Instant unassignedAt,
            String notes) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.incidentId = Objects.requireNonNull(incidentId, "incidentId cannot be null");
        this.assigneeId = Objects.requireNonNull(assigneeId, "assigneeId cannot be null");
        this.assignedBy = Objects.requireNonNull(assignedBy, "assignedBy cannot be null");
        this.assignedAt = Objects.requireNonNull(assignedAt, "assignedAt cannot be null");
        this.unassignedAt = unassignedAt; // Can be null (still assigned)
        this.notes = notes; // Can be null
    }

    public static IncidentAssignment createNew(
            UUID incidentId,
            UUID assigneeId,
            UUID assignedBy,
            String notes) {
        return new IncidentAssignment(
                UUID.randomUUID(),
                incidentId,
                assigneeId,
                assignedBy,
                Instant.now(),
                null,
                notes);
    }

    public static IncidentAssignment of(
            UUID id,
            UUID incidentId,
            UUID assigneeId,
            UUID assignedBy,
            Instant assignedAt,
            Instant unassignedAt,
            String notes) {
        return new IncidentAssignment(id, incidentId, assigneeId, assignedBy, assignedAt, unassignedAt, notes);
    }

    public IncidentAssignment unassign() {
        if (this.unassignedAt != null) {
            throw new IllegalStateException("Assignment is already unassigned");
        }
        return new IncidentAssignment(
                this.id,
                this.incidentId,
                this.assigneeId,
                this.assignedBy,
                this.assignedAt,
                Instant.now(),
                this.notes);
    }

    public IncidentAssignment withNotes(String newNotes) {
        return new IncidentAssignment(
                this.id,
                this.incidentId,
                this.assigneeId,
                this.assignedBy,
                this.assignedAt,
                this.unassignedAt,
                newNotes);
    }

    public boolean isActive() {
        return unassignedAt == null;
    }

    // Getters
    public UUID id() {
        return id;
    }

    public UUID incidentId() {
        return incidentId;
    }

    public UUID assigneeId() {
        return assigneeId;
    }

    public UUID assignedBy() {
        return assignedBy;
    }

    public Instant assignedAt() {
        return assignedAt;
    }

    public Instant unassignedAt() {
        return unassignedAt;
    }

    public String notes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IncidentAssignment that = (IncidentAssignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IncidentAssignment{" +
                "id=" + id +
                ", incidentId=" + incidentId +
                ", assigneeId=" + assigneeId +
                ", assignedBy=" + assignedBy +
                ", active=" + isActive() +
                '}';
    }
}
