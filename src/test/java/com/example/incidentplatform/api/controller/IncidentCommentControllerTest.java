package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.CommentResponse;
import com.example.incidentplatform.api.dto.CreateCommentRequest;
import com.example.incidentplatform.api.dto.UpdateCommentRequest;
import com.example.incidentplatform.application.service.IncidentCommentService;
import com.example.incidentplatform.common.error.GlobalExceptionHandler;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.IncidentComment;
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
class IncidentCommentControllerTest {

    @Mock
    private IncidentCommentService commentService;

    @InjectMocks
    private IncidentCommentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID incidentId;
    private UUID authorId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For Instant serialization

        incidentId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        commentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/incidents/{incidentId}/comments")
    class AddComment {

        @Test
        @DisplayName("should add comment and return 201")
        void shouldAddCommentAndReturn201() throws Exception {
            // Given
            String content = "This is a test comment";
            CreateCommentRequest request = new CreateCommentRequest(content);
            IncidentComment comment = createComment(commentId, incidentId, authorId, content);

            when(commentService.addComment(eq(incidentId), any(UUID.class), eq(content)))
                    .thenReturn(comment);

            // When/Then
            mockMvc.perform(post("/api/incidents/{incidentId}/comments", incidentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .principal(createAuthentication()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(commentId.toString()))
                    .andExpect(jsonPath("$.incidentId").value(incidentId.toString()))
                    .andExpect(jsonPath("$.content").value(content));
        }

        @Test
        @DisplayName("should return 400 when content is blank")
        void shouldReturn400WhenContentBlank() throws Exception {
            // Given
            CreateCommentRequest request = new CreateCommentRequest("");

            // When/Then
            mockMvc.perform(post("/api/incidents/{incidentId}/comments", incidentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .principal(createAuthentication()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when incident not found")
        void shouldReturn404WhenIncidentNotFound() throws Exception {
            // Given
            String content = "This is a test comment";
            CreateCommentRequest request = new CreateCommentRequest(content);

            when(commentService.addComment(eq(incidentId), any(UUID.class), eq(content)))
                    .thenThrow(new NotFoundException("Incident not found"));

            // When/Then
            mockMvc.perform(post("/api/incidents/{incidentId}/comments", incidentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .principal(createAuthentication()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/{incidentId}/comments")
    class GetCommentsForIncident {

        @Test
        @DisplayName("should return comments for incident")
        void shouldReturnCommentsForIncident() throws Exception {
            // Given
            List<IncidentComment> comments = List.of(
                    createComment(UUID.randomUUID(), incidentId, authorId, "First comment"),
                    createComment(UUID.randomUUID(), incidentId, authorId, "Second comment"));
            when(commentService.getCommentsForIncident(incidentId)).thenReturn(comments);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/comments", incidentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].content").value("First comment"))
                    .andExpect(jsonPath("$[1].content").value("Second comment"));
        }

        @Test
        @DisplayName("should return comments sorted descending when sort=desc")
        void shouldReturnCommentsSortedDesc() throws Exception {
            // Given
            List<IncidentComment> comments = List.of(
                    createComment(UUID.randomUUID(), incidentId, authorId, "Newest comment"));
            when(commentService.getCommentsForIncidentNewestFirst(incidentId)).thenReturn(comments);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/comments", incidentId)
                    .param("sort", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].content").value("Newest comment"));

            verify(commentService).getCommentsForIncidentNewestFirst(incidentId);
            verify(commentService, never()).getCommentsForIncident(any());
        }

        @Test
        @DisplayName("should return 404 when incident not found")
        void shouldReturn404WhenIncidentNotFound() throws Exception {
            // Given
            when(commentService.getCommentsForIncident(incidentId))
                    .thenThrow(new NotFoundException("Incident not found"));

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/comments", incidentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/comments/{commentId}")
    class GetComment {

        @Test
        @DisplayName("should return comment when found")
        void shouldReturnCommentWhenFound() throws Exception {
            // Given
            IncidentComment comment = createComment(commentId, incidentId, authorId, "Test content");
            when(commentService.getComment(commentId)).thenReturn(comment);

            // When/Then
            mockMvc.perform(get("/api/comments/{commentId}", commentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(commentId.toString()))
                    .andExpect(jsonPath("$.content").value("Test content"));
        }

        @Test
        @DisplayName("should return 404 when comment not found")
        void shouldReturn404WhenCommentNotFound() throws Exception {
            // Given
            when(commentService.getComment(commentId))
                    .thenThrow(new NotFoundException("Comment not found"));

            // When/Then
            mockMvc.perform(get("/api/comments/{commentId}", commentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/comments/{commentId}")
    class UpdateComment {

        @Test
        @DisplayName("should update comment and return 200")
        void shouldUpdateCommentAndReturn200() throws Exception {
            // Given
            String newContent = "Updated content";
            UpdateCommentRequest request = new UpdateCommentRequest(newContent);
            IncidentComment updated = createComment(commentId, incidentId, authorId, newContent);

            when(commentService.updateComment(commentId, newContent)).thenReturn(updated);

            // When/Then
            mockMvc.perform(put("/api/comments/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value(newContent));
        }

        @Test
        @DisplayName("should return 400 when content is blank")
        void shouldReturn400WhenContentBlank() throws Exception {
            // Given
            UpdateCommentRequest request = new UpdateCommentRequest("");

            // When/Then
            mockMvc.perform(put("/api/comments/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when comment not found")
        void shouldReturn404WhenCommentNotFound() throws Exception {
            // Given
            String newContent = "Updated content";
            UpdateCommentRequest request = new UpdateCommentRequest(newContent);

            when(commentService.updateComment(commentId, newContent))
                    .thenThrow(new NotFoundException("Comment not found"));

            // When/Then
            mockMvc.perform(put("/api/comments/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/comments/{commentId}")
    class DeleteComment {

        @Test
        @DisplayName("should delete comment and return 204")
        void shouldDeleteCommentAndReturn204() throws Exception {
            // Given
            doNothing().when(commentService).deleteComment(commentId);

            // When/Then
            mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                    .andExpect(status().isNoContent());

            verify(commentService).deleteComment(commentId);
        }

        @Test
        @DisplayName("should return 404 when comment not found")
        void shouldReturn404WhenCommentNotFound() throws Exception {
            // Given
            doThrow(new NotFoundException("Comment not found"))
                    .when(commentService).deleteComment(commentId);

            // When/Then
            mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/{incidentId}/comments/count")
    class GetCommentCount {

        @Test
        @DisplayName("should return comment count")
        void shouldReturnCommentCount() throws Exception {
            // Given
            when(commentService.countCommentsForIncident(incidentId)).thenReturn(5L);

            // When/Then
            mockMvc.perform(get("/api/incidents/{incidentId}/comments/count", incidentId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("5"));
        }
    }

    private IncidentComment createComment(UUID id, UUID incidentId, UUID authorId, String content) {
        Instant now = Instant.now();
        return IncidentComment.of(id, incidentId, authorId, content, now, now);
    }

    private Authentication createAuthentication() {
        return new UsernamePasswordAuthenticationToken(authorId.toString(), null, List.of());
    }
}
