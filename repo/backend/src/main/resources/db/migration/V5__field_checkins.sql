CREATE TABLE IF NOT EXISTS check_ins (
    id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL REFERENCES users(id),
    comment_text VARCHAR(4000),
    device_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    server_received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    current_revision_number INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS check_in_revisions (
    id BIGSERIAL PRIMARY KEY,
    check_in_id BIGINT NOT NULL REFERENCES check_ins(id) ON DELETE CASCADE,
    revision_number INTEGER NOT NULL,
    comment_text VARCHAR(4000),
    device_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    changed_fields VARCHAR(500),
    edited_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (check_in_id, revision_number)
);

CREATE TABLE IF NOT EXISTS check_in_attachments (
    id BIGSERIAL PRIMARY KEY,
    check_in_id BIGINT NOT NULL REFERENCES check_ins(id) ON DELETE CASCADE,
    revision_id BIGINT NOT NULL REFERENCES check_in_revisions(id) ON DELETE CASCADE,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    signature_value TEXT NOT NULL,
    signature_algorithm VARCHAR(80) NOT NULL,
    signer_key_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS check_in_audit_events (
    id BIGSERIAL PRIMARY KEY,
    check_in_id BIGINT NOT NULL REFERENCES check_ins(id) ON DELETE CASCADE,
    revision_id BIGINT REFERENCES check_in_revisions(id),
    actor_user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(40) NOT NULL,
    detail VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
