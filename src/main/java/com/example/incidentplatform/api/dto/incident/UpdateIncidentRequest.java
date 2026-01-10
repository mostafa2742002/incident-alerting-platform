package com.example.incidentplatform.api.dto.incident;

public record UpdateIncidentRequest(
        String title, 
        String description, 
        String severity, 
        String status 
) {
    
    public boolean hasUpdates() {
        return title != null || description != null || severity != null || status != null;
    }
}
