CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY,
    org_id VARCHAR(64),
    actor_user_id VARCHAR(64) NOT NULL,
    actor_email VARCHAR(320),
    action_type VARCHAR(80) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(255),
    before_json TEXT,
    after_json TEXT,
    metadata_json TEXT,
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_events_created_at
    ON audit_events (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_events_action_type
    ON audit_events (action_type);

CREATE INDEX IF NOT EXISTS idx_audit_events_actor_user_id
    ON audit_events (actor_user_id);

CREATE INDEX IF NOT EXISTS idx_audit_events_resource
    ON audit_events (resource_type, resource_id);
