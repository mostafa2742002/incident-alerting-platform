package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentCommentRepository;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.IncidentComment;
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
class IncidentCommentServiceTest {

    @Mock
    private IncidentCommentRepository commentRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private IncidentCommentService commentService;

    @Captor
    private ArgumentCaptor<IncidentComment> commentCaptor;

    private UUID incidentId;
    private UUID authorId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        incidentId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        commentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("addComment")
    class AddComment {

        @Test
        @DisplayName("should add comment when incident exists")
        void shouldAddCommentWhenIncidentExists() {
            // Given
            String content = "This is a test comment";
            when(incidentRepository.existsById(incidentId)).thenReturn(true);
            when(commentRepository.save(any(IncidentComment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            IncidentComment result = commentService.addComment(incidentId, authorId, content);

            // Then
            verify(commentRepository).save(commentCaptor.capture());
            IncidentComment saved = commentCaptor.getValue();

            assertThat(saved.incidentId()).isEqualTo(incidentId);
            assertThat(saved.authorId()).isEqualTo(authorId);
            assertThat(saved.content()).isEqualTo(content);
            assertThat(saved.id()).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when incident does not exist")
        void shouldThrowWhenIncidentDoesNotExist() {
            // Given
            when(incidentRepository.existsById(incidentId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> commentService.addComment(incidentId, authorId, "content"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Incident not found");

            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getComment")
    class GetComment {

        @Test
        @DisplayName("should return comment when found")
        void shouldReturnCommentWhenFound() {
            // Given
            IncidentComment comment = createComment(commentId, incidentId, authorId, "Test content");
            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // When
            IncidentComment result = commentService.getComment(commentId);

            // Then
            assertThat(result).isEqualTo(comment);
        }

        @Test
        @DisplayName("should throw NotFoundException when comment not found")
        void shouldThrowWhenCommentNotFound() {
            // Given
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> commentService.getComment(commentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Comment not found");
        }
    }

    @Nested
    @DisplayName("getCommentsForIncident")
    class GetCommentsForIncident {

        @Test
        @DisplayName("should return comments when incident exists")
        void shouldReturnCommentsWhenIncidentExists() {
            // Given
            List<IncidentComment> comments = List.of(
                    createComment(UUID.randomUUID(), incidentId, authorId, "First comment"),
                    createComment(UUID.randomUUID(), incidentId, authorId, "Second comment"));
            when(incidentRepository.existsById(incidentId)).thenReturn(true);
            when(commentRepository.findByIncidentId(incidentId)).thenReturn(comments);

            // When
            List<IncidentComment> result = commentService.getCommentsForIncident(incidentId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(comments);
        }

        @Test
        @DisplayName("should throw NotFoundException when incident does not exist")
        void shouldThrowWhenIncidentDoesNotExist() {
            // Given
            when(incidentRepository.existsById(incidentId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> commentService.getCommentsForIncident(incidentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Incident not found");
        }
    }

    @Nested
    @DisplayName("updateComment")
    class UpdateComment {

        @Test
        @DisplayName("should update comment content")
        void shouldUpdateCommentContent() {
            // Given
            String originalContent = "Original content";
            String newContent = "Updated content";
            IncidentComment original = createComment(commentId, incidentId, authorId, originalContent);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(original));
            when(commentRepository.save(any(IncidentComment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            IncidentComment result = commentService.updateComment(commentId, newContent);

            // Then
            verify(commentRepository).save(commentCaptor.capture());
            IncidentComment saved = commentCaptor.getValue();

            assertThat(saved.content()).isEqualTo(newContent);
            assertThat(saved.id()).isEqualTo(commentId);
            assertThat(saved.incidentId()).isEqualTo(incidentId);
            assertThat(saved.authorId()).isEqualTo(authorId);
        }

        @Test
        @DisplayName("should throw NotFoundException when comment not found")
        void shouldThrowWhenCommentNotFound() {
            // Given
            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(commentId, "new content"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Comment not found");

            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("should delete comment when exists")
        void shouldDeleteCommentWhenExists() {
            // Given
            when(commentRepository.existsById(commentId)).thenReturn(true);

            // When
            commentService.deleteComment(commentId);

            // Then
            verify(commentRepository).deleteById(commentId);
        }

        @Test
        @DisplayName("should throw NotFoundException when comment not found")
        void shouldThrowWhenCommentNotFound() {
            // Given
            when(commentRepository.existsById(commentId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Comment not found");

            verify(commentRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("deleteAllCommentsForIncident")
    class DeleteAllCommentsForIncident {

        @Test
        @DisplayName("should delete all comments for incident")
        void shouldDeleteAllCommentsForIncident() {
            // When
            commentService.deleteAllCommentsForIncident(incidentId);

            // Then
            verify(commentRepository).deleteByIncidentId(incidentId);
        }
    }

    @Nested
    @DisplayName("countCommentsForIncident")
    class CountCommentsForIncident {

        @Test
        @DisplayName("should return comment count")
        void shouldReturnCommentCount() {
            // Given
            when(commentRepository.countByIncidentId(incidentId)).thenReturn(5L);

            // When
            long count = commentService.countCommentsForIncident(incidentId);

            // Then
            assertThat(count).isEqualTo(5L);
        }
    }

    private IncidentComment createComment(UUID id, UUID incidentId, UUID authorId, String content) {
        Instant now = Instant.now();
        return IncidentComment.of(id, incidentId, authorId, content, now, now);
    }
}
