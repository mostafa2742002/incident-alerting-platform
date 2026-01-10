package com.example.incidentplatform.domain.model.notification;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class Notification {

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final UUID incidentId; // Optional - can be null
    private final Instant readAt; // Null if unread
    private final Instant createdAt;
    private final Map<String, Object> metadata; // Additional context

    private Notification(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID incidentId,
            Instant readAt,
            Instant createdAt,
            Map<String, Object> metadata) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.incidentId = incidentId; // Can be null
        this.readAt = readAt; // Can be null (unread)
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public static Notification createNew(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID incidentId,
            Map<String, Object> metadata) {
        return new Notification(
                UUID.randomUUID(),
                userId,
                type,
                title,
                message,
                incidentId,
                null, // Unread
                Instant.now(),
                metadata);
    }


    public static Notification createSimple(
            UUID userId,
            NotificationType type,
            String title,
            String message) {
        return createNew(userId, type, title, message, null, null);
    }

    public static Notification of(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID incidentId,
            Instant readAt,
            Instant createdAt,
            Map<String, Object> metadata) {
        return new Notification(id, userId, type, title, message, incidentId, readAt, createdAt, metadata);
    }


    public Notification markAsRead() {
        if (this.readAt != null) {
            return this; // Already read
        }
        return new Notification(
                this.id,
                this.userId,
                this.type,
                this.title,
                this.message,
                this.incidentId,
                Instant.now(),
                this.createdAt,
                this.metadata);
    }


    public boolean isRead() {
        return readAt != null;
    }

    public boolean isUnread() {
        return readAt == null;
    }

    // Getters
    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public NotificationType type() {
        return type;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    public UUID incidentId() {
        return incidentId;
    }

    public Instant readAt() {
        return readAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", read=" + isRead() +
                '}';
    }
}
