package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.AssignUserRequest;
import com.example.incidentplatform.api.dto.AssignmentResponse;
import com.example.incidentplatform.api.dto.UpdateAssignmentNotesRequest;
import com.example.incidentplatform.application.service.IncidentAssignmentService;
import com.example.incidentplatform.domain.model.IncidentAssignment;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api")
public class IncidentAssignmentController {

    private final IncidentAssignmentService assignmentService;

    public IncidentAssignmentController(IncidentAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/incidents/{incidentId}/assignments")
    public ResponseEntity<AssignmentResponse> assignUser(
            @PathVariable UUID incidentId,
            @Valid @RequestBody AssignUserRequest request,
            Authentication authentication) {

        UUID assignedBy = extractUserId(authentication);

        IncidentAssignment assignment = assignmentService.assignUser(
                incidentId,
                request.assigneeId(),
                assignedBy,
                request.notes());

        return ResponseEntity.status(HttpStatus.CREATED).body(AssignmentResponse.from(assignment));
    }

    @GetMapping("/incidents/{incidentId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsForIncident(
            @PathVariable UUID incidentId,
            @RequestParam(defaultValue = "false") boolean includeHistory) {

        List<IncidentAssignment> assignments;
        if (includeHistory) {
            assignments = assignmentService.getAllAssignmentsForIncident(incidentId);
        } else {
            assignments = assignmentService.getActiveAssignmentsForIncident(incidentId);
        }

        List<AssignmentResponse> response = assignments.stream()
                .map(AssignmentResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/incidents/{incidentId}/assignments/{assigneeId}")
    public ResponseEntity<AssignmentResponse> unassignUser(
            @PathVariable UUID incidentId,
            @PathVariable UUID assigneeId) {

        IncidentAssignment unassigned = assignmentService.unassignUser(incidentId, assigneeId);
        return ResponseEntity.ok(AssignmentResponse.from(unassigned));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<AssignmentResponse> getAssignment(@PathVariable UUID assignmentId) {
        IncidentAssignment assignment = assignmentService.getAssignment(assignmentId);
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<AssignmentResponse> unassignById(@PathVariable UUID assignmentId) {
        IncidentAssignment unassigned = assignmentService.unassignById(assignmentId);
        return ResponseEntity.ok(AssignmentResponse.from(unassigned));
    }

    @PatchMapping("/assignments/{assignmentId}/notes")
    public ResponseEntity<AssignmentResponse> updateNotes(
            @PathVariable UUID assignmentId,
            @RequestBody UpdateAssignmentNotesRequest request) {

        IncidentAssignment updated = assignmentService.updateNotes(assignmentId, request.notes());
        return ResponseEntity.ok(AssignmentResponse.from(updated));
    }

    @GetMapping("/users/{userId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsForUser(@PathVariable UUID userId) {
        List<IncidentAssignment> assignments = assignmentService.getActiveAssignmentsForUser(userId);

        List<AssignmentResponse> response = assignments.stream()
                .map(AssignmentResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/incidents/{incidentId}/assignments/count")
    public ResponseEntity<Long> getAssignmentCount(@PathVariable UUID incidentId) {
        long count = assignmentService.countAssigneesForIncident(incidentId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/incidents/{incidentId}/assignments/{assigneeId}/check")
    public ResponseEntity<Boolean> isUserAssigned(
            @PathVariable UUID incidentId,
            @PathVariable UUID assigneeId) {
        boolean assigned = assignmentService.isUserAssigned(incidentId, assigneeId);
        return ResponseEntity.ok(assigned);
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException e) {
                return UUID.nameUUIDFromBytes(authentication.getName().getBytes());
            }
        }
        throw new IllegalStateException("Unable to determine user ID from authentication");
    }
}
