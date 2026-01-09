package com.example.incidentplatform.domain.model;

/**
 * Status of an incident throughout its lifecycle.
 */
public enum IncidentStatus {
    OPEN, // Newly created, not yet being worked on
    IN_PROGRESS, // Being actively investigated/resolved
    RESOLVED, // Issue is fixed, awaiting closure
    CLOSED // Formally closed, no further action
}
