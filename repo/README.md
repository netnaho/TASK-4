# PharmaProcure Compliance Procurement Portal

PharmaProcure is an offline-capable compliance procurement portal built with Angular, Spring Boot, PostgreSQL, and Docker Compose. It delivers secure authentication, procurement lifecycle management, controlled documents, field evidence check-ins, and dual approval for compliance-critical actions.

## Project Overview

- Full-stack local-network portal for regulated procurement operations
- Production-oriented Angular frontend with responsive enterprise UX
- Spring Boot REST backend with layered architecture and defensive validation
- PostgreSQL persistence with Flyway-managed schema and seed data
- Docker Compose startup with no manual setup requirements

## Start Command

```bash
docker compose up
```

## Service Address

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080`
- Backend health: `http://localhost:8080/api/health`
- Backend version: `http://localhost:8080/api/meta/version`
- PostgreSQL: `localhost:5433`

## Verification Method

1. Start the stack:

   ```bash
   docker compose up --build
   ```

2. Verify frontend and backend health:

   ```bash
   curl http://localhost:8080/api/health
   curl http://localhost:8080/api/meta/version
   curl http://localhost:4200/health
   ```

3. Verify sample flows:

   ```bash
   ./API_tests/auth_api_tests.sh
   ./API_tests/order_lifecycle_api_tests.sh
   ./API_tests/document_center_api_tests.sh
   ./API_tests/checkins_api_tests.sh
   ./API_tests/critical_actions_api_tests.sh
   ```

4. Run the full automated verification suite:

   ```bash
   ./run_tests.sh
   ```

Expected result: all suites pass, services become healthy, and the test summary reports zero failures.

## Sample Test Users / Roles

- Shared password: `PortalAccess2026!`
- Buyer: `buyer1`
- Fulfillment Clerk: `fulfillment1`
- Quality Reviewer: `quality1`
- Finance: `finance1`
- System Administrator: `admin1`
- Secondary Quality Reviewer: `quality2`
- Secondary Finance: `finance2`

## Sample Flow Walkthroughs

### Authentication

- Open `http://localhost:4200`
- Sign in with any seeded account above
- Verify role-specific navigation and access

### Procurement Order Lifecycle

- Buyer creates order in `/orders`
- Buyer submits for review
- Quality Reviewer approves in `/orders/review`
- Finance records payment in `/orders/finance`
- Fulfillment Clerk creates pick/pack and shipment in `/fulfillment`
- Buyer records receipt and discrepancies in `/orders/receipts`
- Buyer manages returns/after-sales in `/orders/returns`

### Document Center

- Buyer creates draft in `/document-center`
- Quality Reviewer approves routed document
- System Administrator archives approved document
- Preview and download actions generate document audit records
- Archived records retain SHA-256 and local server-side signature metadata

### Field Check-Ins

- Buyer creates quick check-in in `/check-ins`
- Optionally capture geolocation if browser allows
- Add image/audio/PDF evidence attachments
- Update the check-in to create a new revision and inspect highlighted changed fields

### Dual Approval

- Buyer requests protected order cancellation from the order detail screen
- Buyer or owner requests document destruction / retention override from document detail
- Quality Reviewer and Finance/System Administrator approve from `/approvals`
- Same-user second approval is rejected
- Requests expire automatically after 24 hours if not fully approved

## Architecture Summary

- Frontend:
  - Angular standalone components
  - `core`, `shared`, `features`, `layout`, guards, interceptors, typed services/models
  - Angular Material + custom premium theme + offline SVG icons
- Backend:
  - Spring Boot 3 / Java 17 / Maven
  - layered packages: `controller`, `service`, `repository`, `dto`, `entity`, `security`, `validation`, `audit`, `config`, `util`
  - session auth, RBAC, CSRF, CAPTCHA, lockout, rate limiting
- Data:
  - PostgreSQL with Flyway migrations
  - backend file volume for document and evidence storage
  - SHA-256 and server-side signature metadata for controlled records/evidence
- Deployment:
  - Docker Compose only
  - one-command startup
  - persistent DB and document storage volumes

## Main API Surface

### Auth

- `GET /api/auth/csrf`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/auth/captcha?username=<username>`

### Orders

- `GET /api/catalog/products`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders`
- `POST /api/orders/{id}/submit-review`
- `POST /api/orders/{id}/cancel`
- `POST /api/orders/{id}/approve`
- `POST /api/orders/{id}/record-payment`
- `POST /api/orders/{id}/pick-pack`
- `POST /api/orders/{id}/shipments`
- `POST /api/orders/{id}/receipts`
- `POST /api/orders/{id}/returns`
- `POST /api/orders/{id}/after-sales-cases`
- `GET /api/orders/{id}/traceability`

### Document Center

- `GET /api/documents/types`
- `GET /api/documents/templates`
- `POST /api/documents/templates`
- `GET /api/documents`
- `GET /api/documents/approval-queue`
- `GET /api/documents/archive`
- `POST /api/documents`
- `PUT /api/documents/{id}`
- `GET /api/documents/{id}`
- `POST /api/documents/{id}/submit-approval`
- `POST /api/documents/{id}/approve`
- `POST /api/documents/{id}/archive`
- `GET /api/documents/{id}/preview`
- `GET /api/documents/{id}/content`
- `GET /api/documents/{id}/download`

### Field Check-Ins

- `GET /api/check-ins`
- `POST /api/check-ins`
- `PUT /api/check-ins/{id}`
- `GET /api/check-ins/{id}`
- `GET /api/check-ins/{id}/attachments/{attachmentId}/download`

### Critical Actions / Dual Approval

- `GET /api/critical-actions`
- `GET /api/critical-actions/{id}`
- `POST /api/critical-actions`
- `POST /api/critical-actions/{id}/decision`

### Admin

- `GET /api/admin/users`
- `GET /api/admin/permissions`
- `GET /api/admin/state-machine`
- `GET /api/admin/document-types`
- `PUT /api/admin/document-types/{id}`
- `GET /api/admin/reason-codes`
- `POST /api/admin/reason-codes`
- `PUT /api/admin/reason-codes/{id}`

## Test Command

```bash
./run_tests.sh
```

## Known Implementation Notes

- PostgreSQL is exposed on `5433` instead of `5432` to avoid local port conflicts observed during validation.
- Document preview watermarking is implemented as a visible overlay during preview/download flows rather than binary PDF rewriting.
- Critical action expiration is enforced in backend service logic when requests are listed, retrieved, or acted upon.
