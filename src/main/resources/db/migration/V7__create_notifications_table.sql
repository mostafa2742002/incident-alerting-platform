-- V7: Create notifications table
-- In-app notifications for users (free alternative to SMS/email services)

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    incident_id UUID REFERENCES incidents(id) ON DELETE CASCADE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

-- Additional metadata as JSON (flexible for different notification types)
metadata JSONB );

-- Index for finding notifications by user (most common query)
CREATE INDEX idx_notifications_user ON notifications (user_id);

-- Index for finding unread notifications
CREATE INDEX idx_notifications_user_unread ON notifications (user_id)
WHERE
    read_at IS NULL;

-- Index for filtering by type
CREATE INDEX idx_notifications_type ON notifications(type);

-- Index for recent notifications
CREATE INDEX idx_notifications_created ON notifications (created_at DESC);

-- Index for incident-related notifications
CREATE INDEX idx_notifications_incident ON notifications (incident_id)
WHERE
    incident_id IS NOT NULL;

COMMENT ON TABLE notifications IS 'In-app notifications for users';

COMMENT ON COLUMN notifications.type IS 'Type: ASSIGNED, UNASSIGNED, STATUS_CHANGED, NEW_COMMENT, ESCALATED, RESOLVED, MENTIONED';

COMMENT ON COLUMN notifications.metadata IS 'Additional context as JSON (e.g., old_status, new_status, commenter_id)';