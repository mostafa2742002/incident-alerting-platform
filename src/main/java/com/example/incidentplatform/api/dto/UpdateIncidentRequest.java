package com.example.incidentplatform.api.dto;

/**
 * DTO for updating an existing incident.
 * All fields are optional - only provided fields will be updated.
 * 
 * Example usage:
 * - Update only status: {"status": "IN_PROGRESS"}
 * - Update multiple: {"status": "RESOLVED", "severity": "LOW"}
 * - Update description: {"description": "Root cause: memory leak in service X"}
 */
public record UpdateIncidentRequest(
        String title, // Optional: new title
        String description, // Optional: new description
        String severity, // Optional: CRITICAL, HIGH, MEDIUM, LOW
        String status // Optional: OPEN, IN_PROGRESS, RESOLVED, CLOSED
) {
    /**
     * Check if any field is provided for update.
     * Prevents empty PATCH requests.
     */
    public boolean hasUpdates() {
        return title != null || description != null || severity != null || status != null;
    }
}
