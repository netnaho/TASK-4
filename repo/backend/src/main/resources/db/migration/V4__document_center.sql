ALTER TABLE document_types
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS evidence_allowed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE document_types
SET description = CASE code
    WHEN 'SOP' THEN 'Standard operating procedure documents'
    WHEN 'PSLIP' THEN 'Packing slip templates and generated records'
    WHEN 'COA' THEN 'Certificates and compliance attestation records'
    ELSE description
END,
evidence_allowed = CASE code
    WHEN 'PSLIP' THEN TRUE
    ELSE FALSE
END;

CREATE TABLE IF NOT EXISTS document_templates (
    id BIGSERIAL PRIMARY KEY,
    document_type_id BIGINT NOT NULL REFERENCES document_types(id),
    template_name VARCHAR(160) NOT NULL,
    template_body TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    document_type_id BIGINT NOT NULL REFERENCES document_types(id),
    template_id BIGINT REFERENCES document_templates(id),
    owner_user_id BIGINT NOT NULL REFERENCES users(id),
    document_number VARCHAR(40) UNIQUE,
    title VARCHAR(220) NOT NULL,
    status VARCHAR(30) NOT NULL,
    metadata_tags VARCHAR(500),
    current_version_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    title_snapshot VARCHAR(220) NOT NULL,
    content_text TEXT,
    original_file_name VARCHAR(255),
    stored_file_name VARCHAR(255),
    storage_path VARCHAR(500),
    mime_type VARCHAR(120),
    file_size_bytes BIGINT,
    sha256_hash VARCHAR(64),
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (document_id, version_number)
);

CREATE TABLE IF NOT EXISTS document_approval_routes (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_id BIGINT NOT NULL REFERENCES document_versions(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_approval_steps (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES document_approval_routes(id) ON DELETE CASCADE,
    step_order INTEGER NOT NULL,
    approver_role VARCHAR(60) NOT NULL,
    approver_user_id BIGINT REFERENCES users(id),
    status VARCHAR(30) NOT NULL,
    comments VARCHAR(1000),
    acted_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (route_id, step_order)
);

CREATE TABLE IF NOT EXISTS document_archives (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_id BIGINT NOT NULL REFERENCES document_versions(id),
    archive_hash VARCHAR(64) NOT NULL,
    signature_value TEXT NOT NULL,
    signature_algorithm VARCHAR(80) NOT NULL,
    signer_key_id VARCHAR(120) NOT NULL,
    archived_by BIGINT NOT NULL REFERENCES users(id),
    archived_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS document_sequences (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(32) NOT NULL,
    sequence_year INTEGER NOT NULL,
    last_sequence_value INTEGER NOT NULL,
    UNIQUE (type_code, sequence_year)
);

CREATE TABLE IF NOT EXISTS document_audit_events (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT REFERENCES documents(id) ON DELETE CASCADE,
    version_id BIGINT REFERENCES document_versions(id),
    actor_user_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(40) NOT NULL,
    detail VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_version FOREIGN KEY (current_version_id) REFERENCES document_versions(id);

INSERT INTO document_templates (document_type_id, template_name, template_body, active, created_by)
SELECT dt.id, 'Default ' || dt.name || ' Template', 'Template starter for ' || dt.name, TRUE, u.id
FROM document_types dt
CROSS JOIN users u
WHERE u.username = 'admin1'
  AND NOT EXISTS (
      SELECT 1 FROM document_templates existing WHERE existing.document_type_id = dt.id AND existing.template_name = 'Default ' || dt.name || ' Template'
  );
