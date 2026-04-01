ALTER TABLE users
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS lockout_until TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS last_failed_login_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP WITH TIME ZONE;

UPDATE users
SET password_hash = '$2b$12$zBv6iNkCO2rpuwTPJljMzujXbSmXhvKR.XTfbJCKQOi7zKA32JdU.'
WHERE username IN ('buyer1', 'fulfillment1', 'quality1', 'finance1', 'admin1');
