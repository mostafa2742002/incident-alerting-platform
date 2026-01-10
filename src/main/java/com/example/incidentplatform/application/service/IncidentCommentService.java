package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentCommentRepository;
import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.IncidentComment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IncidentCommentService {

    private final IncidentCommentRepository commentRepository;
    private final IncidentRepository incidentRepository;

    public IncidentCommentService(IncidentCommentRepository commentRepository,
            IncidentRepository incidentRepository) {
        this.commentRepository = commentRepository;
        this.incidentRepository = incidentRepository;
    }


    public IncidentComment addComment(UUID incidentId, UUID authorId, String content) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new NotFoundException("Incident not found with id: " + incidentId);
        }

        IncidentComment comment = IncidentComment.createNew(incidentId, authorId, content);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public IncidentComment getComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
    }

    @Transactional(readOnly = true)
    public List<IncidentComment> getCommentsForIncident(UUID incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new NotFoundException("Incident not found with id: " + incidentId);
        }

        return commentRepository.findByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public List<IncidentComment> getCommentsForIncidentNewestFirst(UUID incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new NotFoundException("Incident not found with id: " + incidentId);
        }

        return commentRepository.findByIncidentIdNewestFirst(incidentId);
    }

    @Transactional(readOnly = true)
    public List<IncidentComment> getCommentsByAuthor(UUID authorId) {
        return commentRepository.findByAuthorId(authorId);
    }

    public IncidentComment updateComment(UUID commentId, String newContent) {
        IncidentComment existing = getComment(commentId);
        IncidentComment updated = existing.withContent(newContent);
        return commentRepository.save(updated);
    }

    public void deleteComment(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    public void deleteAllCommentsForIncident(UUID incidentId) {
        commentRepository.deleteByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public long countCommentsForIncident(UUID incidentId) {
        return commentRepository.countByIncidentId(incidentId);
    }
}
