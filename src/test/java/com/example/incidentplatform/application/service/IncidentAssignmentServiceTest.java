package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentAssignmentRepository;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.ConflictException;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.IncidentAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentAssignmentServiceTest {

    @Mock
    private IncidentAssignmentRepository assignmentRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private IncidentAssignmentService assignmentService;

    @Captor
    private ArgumentCaptor<IncidentAssignment> assignmentCaptor;

    private UUID incidentId;
    private UUID assigneeId;
    private UUID assignedBy;
    private UUID assignmentId;

    @BeforeEach
    void setUp() {
        incidentId = UUID.randomUUID();
        assigneeId = UUID.randomUUID();
        assignedBy = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("assignUser")
    class AssignUser {

        @Test
        @DisplayName("should assign user when incident exists and user not already assigned")
        void shouldAssignUserSuccessfully() {
            // Given
            String notes = "Primary responder";
            when(incidentRepository.existsById(incidentId)).thenReturn(true);
            when(assignmentRepository.isUserAssigned(incidentId, assigneeId)).thenReturn(false);
            when(assignmentRepository.save(any(IncidentAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            IncidentAssignment result = assignmentService.assignUser(incidentId, assigneeId, assignedBy, notes);

            // Then
            verify(assignmentRepository).save(assignmentCaptor.capture());
            IncidentAssignment saved = assignmentCaptor.getValue();

            assertThat(saved.incidentId()).isEqualTo(incidentId);
            assertThat(saved.assigneeId()).isEqualTo(assigneeId);
            assertThat(saved.assignedBy()).isEqualTo(assignedBy);
            assertThat(saved.notes()).isEqualTo(notes);
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("should throw NotFoundException when incident does not exist")
        void shouldThrowWhenIncidentNotFound() {
            // Given
            when(incidentRepository.existsById(incidentId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> assignmentService.assignUser(incidentId, assigneeId, assignedBy, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Incident not found");

            verify(assignmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ConflictException when user already assigned")
        void shouldThrowWhenUserAlreadyAssigned() {
            // Given
            when(incidentRepository.existsById(incidentId)).thenReturn(true);
            when(assignmentRepository.isUserAssigned(incidentId, assigneeId)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> assignmentService.assignUser(incidentId, assigneeId, assignedBy, null))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already assigned");

            verify(assignmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("unassignUser")
    class UnassignUser {

        @Test
        @DisplayName("should unassign user successfully")
        void shouldUnassignUserSuccessfully() {
            // Given
            IncidentAssignment active = createAssignment(assignmentId, incidentId, assigneeId, assignedBy, null);
            when(assignmentRepository.findActiveAssignment(incidentId, assigneeId)).thenReturn(Optional.of(active));
            when(assignmentRepository.save(any(IncidentAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            IncidentAssignment result = assignmentService.unassignUser(incidentId, assigneeId);

            // Then
            verify(assignmentRepository).save(assignmentCaptor.capture());
            IncidentAssignment saved = assignmentCaptor.getValue();

            assertThat(saved.isActive()).isFalse();
            assertThat(saved.unassignedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when no active assignment found")
        void shouldThrowWhenNoActiveAssignment() {
            // Given
            when(assignmentRepository.findActiveAssignment(incidentId, assigneeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> assignmentService.unassignUser(incidentId, assigneeId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Active assignment not found");
        }
    }

    @Nested
    @DisplayName("unassignById")
    class UnassignById {

        @Test
        @DisplayName("should unassign by ID successfully")
        void shouldUnassignByIdSuccessfully() {
            // Given
            IncidentAssignment active = createAssignment(assignmentId, incidentId, assigneeId, assignedBy, null);
            when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(active));
            when(assignmentRepository.save(any(IncidentAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            IncidentAssignment result = assignmentService.unassignById(assignmentId);

            // Then
            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw ConflictException when assignment already inactive")
        void shouldThrowWhenAlreadyInactive() {
            // Given
            IncidentAssignment inactive = createInactiveAssignment(assignmentId, incidentId, assigneeId, assignedBy);
            when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(inactive));

            // When/Then
            assertThatThrownBy(() -> assignmentService.unassignById(assignmentId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already inactive");
        }
    }

    @Nested
    @DisplayName("getActiveAssignmentsForIncident")
    class GetActiveAssignmentsForIncident {

        @Test
        @DisplayName("should return active assignments")
        void shouldReturnActiveAssignments() {
            // Given
            List<IncidentAssignment> assignments = List.of(
                    createAssignment(UUID.randomUUID(), incidentId, UUID.randomUUID(), assignedBy, null),
                    createAssignment(UUID.randomUUID(), incidentId, UUID.randomUUID(), assignedBy, null));
            when(incidentRepository.existsById(incidentId)).thenReturn(true);
            when(assignmentRepository.findActiveByIncidentId(incidentId)).thenReturn(assignments);

            // When
            List<IncidentAssignment> result = assignmentService.getActiveAssignmentsForIncident(incidentId);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should throw NotFoundException when incident not found")
        void shouldThrowWhenIncidentNotFound() {
            // Given
            when(incidentRepository.existsById(incidentId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> assignmentService.getActiveAssignmentsForIncident(incidentId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getActiveAssignmentsForUser")
    class GetActiveAssignmentsForUser {

        @Test
        @DisplayName("should return assignments for user")
        void shouldReturnAssignmentsForUser() {
            // Given
            List<IncidentAssignment> assignments = List.of(
                    createAssignment(UUID.randomUUID(), UUID.randomUUID(), assigneeId, assignedBy, null));
            when(assignmentRepository.findActiveByAssigneeId(assigneeId)).thenReturn(assignments);

            // When
            List<IncidentAssignment> result = assignmentService.getActiveAssignmentsForUser(assigneeId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).assigneeId()).isEqualTo(assigneeId);
        }
    }

    @Nested
    @DisplayName("updateNotes")
    class UpdateNotes {

        @Test
        @DisplayName("should update notes successfully")
        void shouldUpdateNotesSuccessfully() {
            // Given
            String newNotes = "Updated: Escalation lead";
            IncidentAssignment existing = createAssignment(assignmentId, incidentId, assigneeId, assignedBy,
                    "Old notes");
            when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(existing));
            when(assignmentRepository.save(any(IncidentAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            IncidentAssignment result = assignmentService.updateNotes(assignmentId, newNotes);

            // Then
            verify(assignmentRepository).save(assignmentCaptor.capture());
            assertThat(assignmentCaptor.getValue().notes()).isEqualTo(newNotes);
        }
    }

    @Nested
    @DisplayName("countMethods")
    class CountMethods {

        @Test
        @DisplayName("should count assignees for incident")
        void shouldCountAssigneesForIncident() {
            // Given
            when(assignmentRepository.countActiveByIncidentId(incidentId)).thenReturn(3L);

            // When
            long count = assignmentService.countAssigneesForIncident(incidentId);

            // Then
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("should count incidents for user")
        void shouldCountIncidentsForUser() {
            // Given
            when(assignmentRepository.countActiveByAssigneeId(assigneeId)).thenReturn(5L);

            // When
            long count = assignmentService.countIncidentsForUser(assigneeId);

            // Then
            assertThat(count).isEqualTo(5L);
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
}
