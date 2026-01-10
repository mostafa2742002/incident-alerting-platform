-- V6: Create incident_assignments table
-- Tracks which users are assigned to which incidents

CREATE TABLE incident_assignments (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    assignee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    unassigned_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,

-- Prevent duplicate active assignments
CONSTRAINT unique_active_assignment UNIQUE (incident_id, assignee_id)
);

-- Index for finding assignments by incident
CREATE INDEX idx_incident_assignments_incident ON incident_assignments (incident_id);

-- Index for finding assignments by assignee (what incidents am I assigned to?)
CREATE INDEX idx_incident_assignments_assignee ON incident_assignments (assignee_id);

-- Index for finding who made assignments
CREATE INDEX idx_incident_assignments_assigned_by ON incident_assignments (assigned_by);

-- Index for finding active assignments (not unassigned)
CREATE INDEX idx_incident_assignments_active ON incident_assignments (incident_id)
WHERE
    unassigned_at IS NULL;

COMMENT ON
TABLE incident_assignments IS 'Tracks user assignments to incidents';

COMMENT ON COLUMN incident_assignments.notes IS 'Optional notes about why the user was assigned';

COMMENT ON COLUMN incident_assignments.unassigned_at IS 'When the user was unassigned (NULL if still assigned)';