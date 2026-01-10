package com.example.incidentplatform.domain.model.webhook;

/**
 * Events that can trigger webhook notifications.
 */
public enum WebhookEventType {
    INCIDENT_CREATED,
    INCIDENT_UPDATED,
    INCIDENT_RESOLVED,
    INCIDENT_CLOSED,
    INCIDENT_ASSIGNED,
    INCIDENT_UNASSIGNED,
    INCIDENT_ESCALATED,
    COMMENT_ADDED
}
