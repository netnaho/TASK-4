# API Tests

- `auth_api_tests.sh` validates login success/failure, CAPTCHA escalation, lockout, unauthorized access, and role-based endpoint access.
- `order_lifecycle_api_tests.sh` validates the end-to-end procurement order lifecycle and permission boundaries.
- `document_center_api_tests.sh` validates controlled templates, draft/approval/archive flow, preview metadata, download audit, numbering, and file validation.
- `checkins_api_tests.sh` validates field check-in creation, optional geolocation, evidence uploads, revision trail preservation, and download audit.
- `critical_actions_api_tests.sh` validates dual approval with two users, same-user rejection, expiration after 24 hours, and protected action execution.
- `phase1_smoke.http` keeps quick manual endpoint checks for local inspection.
