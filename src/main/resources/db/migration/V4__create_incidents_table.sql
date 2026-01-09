-- V4__create_incidents_table.sql
-- Create incidents table for tracking issues within tenants

CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(32) NOT NULL CHECK (
        severity IN (
            'CRITICAL',
            'HIGH',
            'MEDIUM',
            'LOW'
        )
    ),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN' CHECK (
        status IN (
            'OPEN',
            'IN_PROGRESS',
            'RESOLVED',
            'CLOSED'
        )
    ),
    created_by UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_incidents_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT fk_incidents_user FOREIGN KEY (created_by) REFERENCES users (id)
);

-- Create indexes for common queries
CREATE INDEX idx_incidents_tenant ON incidents (tenant_id);

CREATE INDEX idx_incidents_tenant_status ON incidents (tenant_id, status);

CREATE INDEX idx_incidents_created_by ON incidents (created_by);