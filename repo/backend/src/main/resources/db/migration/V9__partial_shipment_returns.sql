INSERT INTO order_state_machine_definitions (from_status, to_status, active)
VALUES ('PARTIALLY_SHIPPED', 'RETURNED', true)
ON CONFLICT (from_status, to_status) DO NOTHING;
