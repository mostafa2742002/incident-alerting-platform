-- V5: Incident Comments
-- Allows team members to add notes, updates, and findings to incidents

CREATE TABLE incident_comments (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for fast lookup of comments by incident
CREATE INDEX idx_incident_comments_incident_id ON incident_comments(incident_id);

-- Index for finding all comments by a user (useful for activity tracking)
CREATE INDEX idx_incident_comments_author_id ON incident_comments(author_id);

-- Comments should be ordered by creation time (oldest first typically)
CREATE INDEX idx_incident_comments_created_at ON incident_comments(incident_id, created_at);

COMMENT ON TABLE incident_comments IS 'Comments and investigation notes attached to incidents';
COMMENT ON COLUMN incident_comments.content IS 'The comment text - investigation notes, findings, status updates';
