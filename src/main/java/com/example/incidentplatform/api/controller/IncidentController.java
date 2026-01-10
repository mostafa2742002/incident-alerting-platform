package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.usecase.ManageIncidentUseCase;
import com.example.incidentplatform.api.dto.CreateIncidentRequest;
import com.example.incidentplatform.api.dto.UpdateIncidentRequest;
import com.example.incidentplatform.api.dto.IncidentResponse;
import com.example.incidentplatform.domain.model.IncidentStatus;
import com.example.incidentplatform.domain.model.Severity;
import com.example.incidentplatform.infrastructure.security.SecurityContextHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/incidents")
public class IncidentController {

    private final ManageIncidentUseCase manageIncidentUseCase;
    private final SecurityContextHelper securityContextHelper;

    public IncidentController(ManageIncidentUseCase manageIncidentUseCase,
            SecurityContextHelper securityContextHelper) {
        this.manageIncidentUseCase = manageIncidentUseCase;
        this.securityContextHelper = securityContextHelper;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @PathVariable UUID tenantId,
            @RequestBody CreateIncidentRequest request) {

        UUID currentUserId = securityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new com.example.incidentplatform.common.error.ForbiddenException(
                        "User not authenticated"));

        var incident = manageIncidentUseCase.createIncident(
                tenantId,
                request.title(),
                request.description(),
                request.severity(),
                currentUserId);

        return ResponseEntity.status(201).body(toResponse(incident));
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentResponse> getIncident(
            @PathVariable UUID tenantId,
            @PathVariable UUID incidentId) {

        var incident = manageIncidentUseCase.getIncident(tenantId, incidentId);
        return ResponseEntity.ok(toResponse(incident));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> listIncidents(@PathVariable UUID tenantId) {
        var incidents = manageIncidentUseCase.listIncidents(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<IncidentResponse>> listIncidentsByStatus(
            @PathVariable UUID tenantId,
            @PathVariable String status) {

        IncidentStatus incidentStatus = IncidentStatus.valueOf(status.toUpperCase());
        var incidents = manageIncidentUseCase.listIncidentsByStatus(tenantId, incidentStatus).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(incidents);
    }

    @DeleteMapping("/{incidentId}")
    public ResponseEntity<Void> deleteIncident(
            @PathVariable UUID tenantId,
            @PathVariable UUID incidentId) {

        manageIncidentUseCase.deleteIncident(tenantId, incidentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{incidentId}")
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable UUID tenantId,
            @PathVariable UUID incidentId,
            @RequestBody UpdateIncidentRequest request) {

        if (!request.hasUpdates()) {
            return ResponseEntity.badRequest().build();
        }

        Severity severity = null;
        if (request.severity() != null) {
            severity = Severity.valueOf(request.severity().toUpperCase());
        }

        IncidentStatus status = null;
        if (request.status() != null) {
            status = IncidentStatus.valueOf(request.status().toUpperCase());
        }

        var incident = manageIncidentUseCase.updateIncident(
                tenantId,
                incidentId,
                request.title(),
                request.description(),
                severity,
                status);

        return ResponseEntity.ok(toResponse(incident));
    }

    @PostMapping("/{incidentId}/escalate")
    public ResponseEntity<IncidentResponse> escalateIncident(
            @PathVariable UUID tenantId,
            @PathVariable UUID incidentId) {

        var incident = manageIncidentUseCase.escalateIncident(tenantId, incidentId);
        return ResponseEntity.ok(toResponse(incident));
    }

    // Helper method to convert domain model to response DTO
    private IncidentResponse toResponse(com.example.incidentplatform.domain.model.Incident incident) {
        return new IncidentResponse(
                incident.id(),
                incident.tenantId(),
                incident.title(),
                incident.description(),
                incident.severity(),
                incident.status(),
                incident.createdBy(),
                incident.createdAt(),
                incident.updatedAt(),
                incident.resolvedAt());
    }
}
