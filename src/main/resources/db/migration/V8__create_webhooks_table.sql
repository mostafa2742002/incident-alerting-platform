-- V8: Create webhooks table for external integrations
CREATE TABLE webhooks (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255),
    events VARCHAR(500) NOT NULL, -- comma-separated list of event types
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        last_triggered_at TIMESTAMP
    WITH
        TIME ZONE,
        failure_count INTEGER NOT NULL DEFAULT 0,
        CONSTRAINT fk_webhook_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

CREATE INDEX idx_webhooks_tenant_id ON webhooks (tenant_id);

CREATE INDEX idx_webhooks_active ON webhooks (tenant_id, is_active);

-- Webhook delivery log for tracking webhook calls
CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY,
    webhook_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    response_status INTEGER,
    response_body TEXT,
    delivered_at TIMESTAMP
    WITH
        TIME ZONE NOT NULL DEFAULT NOW(),
        success BOOLEAN NOT NULL,
        error_message TEXT,
        CONSTRAINT fk_delivery_webhook FOREIGN KEY (webhook_id) REFERENCES webhooks (id) ON DELETE CASCADE
);

CREATE INDEX idx_webhook_deliveries_webhook_id ON webhook_deliveries (webhook_id);

CREATE INDEX idx_webhook_deliveries_delivered_at ON webhook_deliveries (delivered_at);