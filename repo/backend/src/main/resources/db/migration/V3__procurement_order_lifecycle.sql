CREATE TABLE IF NOT EXISTS order_state_machine_definitions (
    id BIGSERIAL PRIMARY KEY,
    from_status VARCHAR(40) NOT NULL,
    to_status VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (from_status, to_status)
);

CREATE TABLE IF NOT EXISTS procurement_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    current_status VARCHAR(40) NOT NULL,
    payment_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    review_required BOOLEAN NOT NULL DEFAULT TRUE,
    review_completed_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    payment_recorded_at TIMESTAMP WITH TIME ZONE,
    pick_pack_started_at TIMESTAMP WITH TIME ZONE,
    last_shipped_at TIMESTAMP WITH TIME ZONE,
    last_received_at TIMESTAMP WITH TIME ZONE,
    returned_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS procurement_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product_catalog(id),
    product_name_snapshot VARCHAR(180) NOT NULL,
    sku_snapshot VARCHAR(80) NOT NULL,
    unit_snapshot VARCHAR(32) NOT NULL,
    unit_price_snapshot NUMERIC(14,2) NOT NULL,
    ordered_quantity INTEGER NOT NULL,
    shipped_quantity INTEGER NOT NULL DEFAULT 0,
    received_quantity INTEGER NOT NULL DEFAULT 0,
    returned_quantity INTEGER NOT NULL DEFAULT 0,
    damaged_quantity INTEGER NOT NULL DEFAULT 0,
    discrepancy_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS order_reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES procurement_orders(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    decision VARCHAR(30) NOT NULL,
    comments VARCHAR(1000),
    reviewed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES procurement_orders(id) ON DELETE CASCADE,
    finance_user_id BIGINT NOT NULL REFERENCES users(id),
    reference_number VARCHAR(120) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    shipment_number VARCHAR(40) NOT NULL UNIQUE,
    fulfillment_user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL,
    shipped_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS shipment_items (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    order_item_id BIGINT NOT NULL REFERENCES procurement_order_items(id),
    quantity INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS receipts (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    receipt_number VARCHAR(40) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    has_discrepancy BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS receipt_items (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL REFERENCES receipts(id) ON DELETE CASCADE,
    order_item_id BIGINT NOT NULL REFERENCES procurement_order_items(id),
    quantity INTEGER NOT NULL,
    discrepancy_reason VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS order_returns (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    return_number VARCHAR(40) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    reason_code VARCHAR(40) NOT NULL,
    comments VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS return_items (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL REFERENCES order_returns(id) ON DELETE CASCADE,
    order_item_id BIGINT NOT NULL REFERENCES procurement_order_items(id),
    quantity INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS after_sales_cases (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    order_item_id BIGINT REFERENCES procurement_order_items(id),
    case_number VARCHAR(40) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    reason_code VARCHAR(40) NOT NULL,
    structured_detail VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    actor_user_id BIGINT NOT NULL REFERENCES users(id),
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    detail VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO order_state_machine_definitions (from_status, to_status, active)
VALUES
    ('CREATED', 'UNDER_REVIEW', true),
    ('UNDER_REVIEW', 'APPROVED', true),
    ('UNDER_REVIEW', 'CANCELED', true),
    ('APPROVED', 'PAYMENT_RECORDED', true),
    ('PAYMENT_RECORDED', 'PICK_PACK', true),
    ('PICK_PACK', 'SHIPPED', true),
    ('SHIPPED', 'RECEIVED', true),
    ('SHIPPED', 'RETURNED', true),
    ('RECEIVED', 'RETURNED', true)
ON CONFLICT (from_status, to_status) DO NOTHING;

INSERT INTO users (username, password_hash, display_name, role_id, active)
VALUES
    ('quality2', '$2b$12$zBv6iNkCO2rpuwTPJljMzujXbSmXhvKR.XTfbJCKQOi7zKA32JdU.', 'Quality Two', (SELECT id FROM roles WHERE name = 'QUALITY_REVIEWER'), true),
    ('finance2', '$2b$12$zBv6iNkCO2rpuwTPJljMzujXbSmXhvKR.XTfbJCKQOi7zKA32JdU.', 'Finance Two', (SELECT id FROM roles WHERE name = 'FINANCE'), true)
ON CONFLICT (username) DO NOTHING;
