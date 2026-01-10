package com.example.incidentplatform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class IncidentComment {

    private final UUID id;
    private final UUID incidentId;
    private final UUID authorId;
    private final String content;
    private final Instant createdAt;
    private final Instant updatedAt;

    private IncidentComment(
            UUID id,
            UUID incidentId,
            UUID authorId,
            String content,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.incidentId = Objects.requireNonNull(incidentId, "incidentId cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "authorId cannot be null");
        this.content = Objects.requireNonNull(content, "content cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");

        if (content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be blank");
        }
    }

    public static IncidentComment createNew(UUID incidentId, UUID authorId, String content) {
        Instant now = Instant.now();
        return new IncidentComment(
                UUID.randomUUID(),
                incidentId,
                authorId,
                content.trim(),
                now,
                now);
    }

    public static IncidentComment of(
            UUID id,
            UUID incidentId,
            UUID authorId,
            String content,
            Instant createdAt,
            Instant updatedAt) {
        return new IncidentComment(id, incidentId, authorId, content, createdAt, updatedAt);
    }

    public IncidentComment withContent(String newContent) {
        return new IncidentComment(
                this.id,
                this.incidentId,
                this.authorId,
                newContent.trim(),
                this.createdAt,
                Instant.now());
    }

    // Getters
    public UUID id() {
        return id;
    }

    public UUID incidentId() {
        return incidentId;
    }

    public UUID authorId() {
        return authorId;
    }

    public String content() {
        return content;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public boolean wasEdited() {
        return !createdAt.equals(updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IncidentComment that = (IncidentComment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IncidentComment{" +
                "id=" + id +
                ", incidentId=" + incidentId +
                ", authorId=" + authorId +
                ", contentLength=" + content.length() +
                ", createdAt=" + createdAt +
                '}';
    }
}
