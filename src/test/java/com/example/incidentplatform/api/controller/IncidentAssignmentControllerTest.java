package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.assign.AssignUserRequest;
import com.example.incidentplatform.api.dto.assign.UpdateAssignmentNotesRequest;
import com.example.incidentplatform.application.service.IncidentAssignmentService;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.common.error.GlobalExceptionHandler;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.incident.IncidentAssignment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class IncidentAssignmentControllerTest {

    @Mock
    private IncidentAssignmentService assignmentService;

    @InjectMocks
    private IncidentAssignmentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID incidentId;
    private UUID assigneeId;
    private UUID assignedBy;
    private UUID assignmentId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        incidentId = UUID.randomUUID();
        assigneeId = UUID.randomUUID();
        assignedBy = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/incidents/{incidentId}/assignments")
    class AssignUser {

        @Test
        @DisplayName("should assign user and return 201")
        void shouldAssignUserAndReturn201() throws Exception {
            // Given
            AssignUserRequest request = new AssignUserRequest(assigneeId, "Primary responder");
            IncidentAssignment assignment = createAssignment(assignmentId, incidentId, assigneeId, assignedBy,
                    "Primary responder");

            when(assignmentService.assignUser(eq(incidentId), eq(assigneeId), any(UUID.class), eq("Primary responder")))
                    .thenReturn(assignment);

            // When/Then
            mockMvc.perform(post("/api/incidents/{incidentId}/assignments", incidentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .principal(createAuthentication()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.incidentId").value(incidentId.toString()))
                    .andExpect(jsonPath("$.assigneeId").value(assigneeId.toString()))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("should return 404 when incident not found")
        void shouldReturn404WhenIncidentNotFound() throws Exception {
            // Given
            AssignUserRequest request = new AssignUserRequest(assigneeId, null);

            when(assignmentService.assignUser(eq(incidentId), eq(assigneeId), any(UUID.class), any()))
                    .thenThrow(new NotFoundException("Incident not found"));

            // When/Then
            mockMvc.perform(post("/api/incidents/{incidentId}/assignments", incidentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .principal(createAuthentication()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 409 when user already assigned")
        void shouldReturn409WhenUserAlreadyAssigned() throws Exception {
            // Given
            AssignUserRequest request = new AssignUserRequest(assigneeId, null);

            when(assignmentService.assignUser(eq(incidentId), eq(assigneeId), any(UUID.class), any()))
                    .thenThrow(new ConflictException("User is already assigned"));

            // When/Then
            mockMvc.perform(post("/api/incidents/{incidentId}/assignments", incidentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .principal(createAuthentication()))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/{incidentId}/assignments")
    class GetAssignmentsForIncident {

        @Test
        @DisplayName("should return active assignments by default")
        void shouldReturnActiveAssignments() throws Exception {
            // Given
            List<IncidentAssignment> assignments = List.of(
                    createAssignment(UUID.randomUUID(), incidentId, UUID.randomUUID(), assignedBy, null));
            when(assignmentService.getActiveAssignmentsForIncident(incidentId)).thenReturn(assignments);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/assignments", incidentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(assignmentService).getActiveAssignmentsForIncident(incidentId);
            verify(assignmentService, never()).getAllAssignmentsForIncident(any());
        }

        @Test
        @DisplayName("should return all assignments when includeHistory=true")
        void shouldReturnAllAssignmentsWithHistory() throws Exception {
            // Given
            List<IncidentAssignment> assignments = List.of(
                    createAssignment(UUID.randomUUID(), incidentId, UUID.randomUUID(), assignedBy, null));
            when(assignmentService.getAllAssignmentsForIncident(incidentId)).thenReturn(assignments);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/assignments", incidentId)
                    .param("includeHistory", "true"))
                    .andExpect(status().isOk());

            verify(assignmentService).getAllAssignmentsForIncident(incidentId);
            verify(assignmentService, never()).getActiveAssignmentsForIncident(any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/incidents/{incidentId}/assignments/{assigneeId}")
    class UnassignUser {

        @Test
        @DisplayName("should unassign user and return 200")
        void shouldUnassignUserAndReturn200() throws Exception {
            // Given
            IncidentAssignment unassigned = createInactiveAssignment(assignmentId, incidentId, assigneeId, assignedBy);
            when(assignmentService.unassignUser(incidentId, assigneeId)).thenReturn(unassigned);

            // When/Then
            mockMvc.perform(delete("/api/incidents/{incidentId}/assignments/{assigneeId}", incidentId, assigneeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @DisplayName("should return 404 when assignment not found")
        void shouldReturn404WhenAssignmentNotFound() throws Exception {
            // Given
            when(assignmentService.unassignUser(incidentId, assigneeId))
                    .thenThrow(new NotFoundException("Active assignment not found"));

            // When/Then
            mockMvc.perform(delete("/api/incidents/{incidentId}/assignments/{assigneeId}", incidentId, assigneeId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/assignments/{assignmentId}")
    class GetAssignment {

        @Test
        @DisplayName("should return assignment when found")
        void shouldReturnAssignmentWhenFound() throws Exception {
            // Given
            IncidentAssignment assignment = createAssignment(assignmentId, incidentId, assigneeId, assignedBy, "Notes");
            when(assignmentService.getAssignment(assignmentId)).thenReturn(assignment);

            // When/Then
            mockMvc.perform(get("/api/assignments/{assignmentId}", assignmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.notes").value("Notes"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/assignments/{assignmentId}/notes")
    class UpdateNotes {

        @Test
        @DisplayName("should update notes and return 200")
        void shouldUpdateNotesAndReturn200() throws Exception {
            // Given
            String newNotes = "Updated notes";
            UpdateAssignmentNotesRequest request = new UpdateAssignmentNotesRequest(newNotes);
            IncidentAssignment updated = createAssignment(assignmentId, incidentId, assigneeId, assignedBy, newNotes);

            when(assignmentService.updateNotes(assignmentId, newNotes)).thenReturn(updated);

            // When/Then
            mockMvc.perform(patch("/api/assignments/{assignmentId}/notes", assignmentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes").value(newNotes));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{userId}/assignments")
    class GetAssignmentsForUser {

        @Test
        @DisplayName("should return assignments for user")
        void shouldReturnAssignmentsForUser() throws Exception {
            // Given
            List<IncidentAssignment> assignments = List.of(
                    createAssignment(UUID.randomUUID(), UUID.randomUUID(), assigneeId, assignedBy, null));
            when(assignmentService.getActiveAssignmentsForUser(assigneeId)).thenReturn(assignments);

            // When/Then
            mockMvc.perform(get("/api/users/{userId}/assignments", assigneeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/{incidentId}/assignments/count")
    class GetAssignmentCount {

        @Test
        @DisplayName("should return assignment count")
        void shouldReturnAssignmentCount() throws Exception {
            // Given
            when(assignmentService.countAssigneesForIncident(incidentId)).thenReturn(3L);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/assignments/count", incidentId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("3"));
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/{incidentId}/assignments/{assigneeId}/check")
    class IsUserAssigned {

        @Test
        @DisplayName("should return true when user is assigned")
        void shouldReturnTrueWhenAssigned() throws Exception {
            // Given
            when(assignmentService.isUserAssigned(incidentId, assigneeId)).thenReturn(true);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/assignments/{assigneeId}/check", incidentId, assigneeId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }
    }

    private IncidentAssignment createAssignment(UUID id, UUID incidentId, UUID assigneeId, UUID assignedBy,
            String notes) {
        return IncidentAssignment.of(id, incidentId, assigneeId, assignedBy, Instant.now(), null, notes);
    }

    private IncidentAssignment createInactiveAssignment(UUID id, UUID incidentId, UUID assigneeId, UUID assignedBy) {
        return IncidentAssignment.of(id, incidentId, assigneeId, assignedBy, Instant.now().minusSeconds(3600),
                Instant.now(), null);
    }

    private Authentication createAuthentication() {
        return new UsernamePasswordAuthenticationToken(assignedBy.toString(), null, List.of());
    }
}
