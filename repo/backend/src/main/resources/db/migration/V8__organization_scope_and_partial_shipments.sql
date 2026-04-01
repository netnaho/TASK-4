ALTER TABLE users
    ADD COLUMN IF NOT EXISTS organization_code VARCHAR(64) NOT NULL DEFAULT 'ORG-ALPHA';

UPDATE users
SET organization_code = 'PLATFORM'
WHERE username = 'admin1';

UPDATE users
SET organization_code = 'ORG-BETA'
WHERE username IN ('quality2', 'finance2');

UPDATE users
SET organization_code = 'ORG-ALPHA'
WHERE username IN ('buyer1', 'fulfillment1', 'quality1', 'finance1');

INSERT INTO order_state_machine_definitions (from_status, to_status, active)
VALUES
    ('PICK_PACK', 'PARTIALLY_SHIPPED', true),
    ('PARTIALLY_SHIPPED', 'SHIPPED', true)
ON CONFLICT (from_status, to_status) DO NOTHING;
