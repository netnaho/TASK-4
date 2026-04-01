CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS product_catalog (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    unit_price NUMERIC(14,2) NOT NULL,
    unit VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS document_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO roles (name)
VALUES
    ('BUYER'),
    ('FULFILLMENT_CLERK'),
    ('QUALITY_REVIEWER'),
    ('FINANCE'),
    ('SYSTEM_ADMINISTRATOR')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, password_hash, display_name, role_id, active)
VALUES
    ('buyer1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5mM6z6i0s5momkMumZ5qX6Ch12Haxaa', 'Buyer One', (SELECT id FROM roles WHERE name = 'BUYER'), true),
    ('fulfillment1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5mM6z6i0s5momkMumZ5qX6Ch12Haxaa', 'Fulfillment One', (SELECT id FROM roles WHERE name = 'FULFILLMENT_CLERK'), true),
    ('quality1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5mM6z6i0s5momkMumZ5qX6Ch12Haxaa', 'Quality One', (SELECT id FROM roles WHERE name = 'QUALITY_REVIEWER'), true),
    ('finance1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5mM6z6i0s5momkMumZ5qX6Ch12Haxaa', 'Finance One', (SELECT id FROM roles WHERE name = 'FINANCE'), true),
    ('admin1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5mM6z6i0s5momkMumZ5qX6Ch12Haxaa', 'System Admin', (SELECT id FROM roles WHERE name = 'SYSTEM_ADMINISTRATOR'), true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO product_catalog (sku, name, unit_price, unit)
VALUES
    ('PP-ACET-500', 'Acetaminophen 500mg Tablets', 8.90, 'box'),
    ('PP-NACL-09', 'Sodium Chloride 0.9% 500ml', 3.25, 'bag'),
    ('PP-GAUZE-4', 'Sterile Gauze Pads 4x4', 12.40, 'pack')
ON CONFLICT (sku) DO NOTHING;

INSERT INTO document_types (code, name, active)
VALUES
    ('SOP', 'Standard Operating Procedure', true),
    ('PSLIP', 'Packing Slip', true),
    ('COA', 'Certificate of Analysis', true)
ON CONFLICT (code) DO NOTHING;
