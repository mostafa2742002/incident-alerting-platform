package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.comment.CommentResponse;
import com.example.incidentplatform.api.dto.comment.CreateCommentRequest;
import com.example.incidentplatform.api.dto.comment.UpdateCommentRequest;
import com.example.incidentplatform.application.service.IncidentCommentService;
import com.example.incidentplatform.domain.model.incident.IncidentComment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api")
public class IncidentCommentController {

    private final IncidentCommentService commentService;

    public IncidentCommentController(IncidentCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/incidents/{incidentId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID incidentId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        UUID authorId = extractUserId(authentication);

        IncidentComment comment = commentService.addComment(incidentId, authorId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    @GetMapping("/incidents/{incidentId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsForIncident(
            @PathVariable UUID incidentId,
            @RequestParam(defaultValue = "asc") String sort) {

        List<IncidentComment> comments;
        if ("desc".equalsIgnoreCase(sort)) {
            comments = commentService.getCommentsForIncidentNewestFirst(incidentId);
        } else {
            comments = commentService.getCommentsForIncident(incidentId);
        }

        List<CommentResponse> response = comments.stream()
                .map(CommentResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable UUID commentId) {
        IncidentComment comment = commentService.getComment(commentId);
        return ResponseEntity.ok(CommentResponse.from(comment));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        IncidentComment updated = commentService.updateComment(commentId, request.content());
        return ResponseEntity.ok(CommentResponse.from(updated));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/incidents/{incidentId}/comments/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable UUID incidentId) {
        long count = commentService.countCommentsForIncident(incidentId);
        return ResponseEntity.ok(count);
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
