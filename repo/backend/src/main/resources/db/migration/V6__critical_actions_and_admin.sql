ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS retention_override_until TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS destroyed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS destroyed_by BIGINT REFERENCES users(id);

CREATE TABLE IF NOT EXISTS reason_codes (
    id BIGSERIAL PRIMARY KEY,
    code_type VARCHAR(40) NOT NULL,
    code VARCHAR(60) NOT NULL,
    label VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (code_type, code)
);

INSERT INTO reason_codes (code_type, code, label, active)
VALUES
    ('RETURN', 'DAMAGED_GOODS', 'Damaged goods', TRUE),
    ('RETURN', 'TEMPERATURE_EXCURSION', 'Temperature excursion', TRUE),
    ('RETURN', 'OTHER', 'Other', TRUE),
    ('AFTER_SALES', 'DAMAGED_GOODS', 'Damaged goods', TRUE),
    ('AFTER_SALES', 'TEMPERATURE_EXCURSION', 'Temperature excursion', TRUE),
    ('AFTER_SALES', 'OTHER', 'Other', TRUE)
ON CONFLICT (code_type, code) DO NOTHING;

CREATE TABLE IF NOT EXISTS critical_action_requests (
    id BIGSERIAL PRIMARY KEY,
    request_type VARCHAR(60) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id BIGINT NOT NULL,
    justification VARCHAR(2000) NOT NULL,
    requested_by BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolution_note VARCHAR(1000),
    UNIQUE (request_type, target_type, target_id, status)
);

CREATE TABLE IF NOT EXISTS critical_action_approvals (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES critical_action_requests(id) ON DELETE CASCADE,
    approver_user_id BIGINT NOT NULL REFERENCES users(id),
    decision VARCHAR(20) NOT NULL,
    comments VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (request_id, approver_user_id)
);

CREATE TABLE IF NOT EXISTS critical_action_audit_events (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES critical_action_requests(id) ON DELETE CASCADE,
    actor_user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(40) NOT NULL,
    detail VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
