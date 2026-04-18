# PharmaProcure Compliance Procurement Portal

**Project type: fullstack**

PharmaProcure is an offline-capable compliance procurement portal built with Angular, Spring Boot, PostgreSQL, and Docker Compose. It delivers secure authentication, procurement lifecycle management, controlled documents, field evidence check-ins, and dual approval for compliance-critical actions.

## Project Overview

- fullstack local-network portal for regulated procurement operations
- Production-oriented Angular frontend with responsive enterprise UX
- Spring Boot REST backend with layered architecture and defensive validation
- PostgreSQL persistence with Flyway-managed schema and seed data
- Docker Compose startup with no manual setup requirements

## Start Command

Start the entire stack with a single command:

```bash
docker-compose up
```

The modern Docker CLI alias is also supported:

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

The only host prerequisites are **bash** and **Docker / Docker Compose v2**. No host-local `python3`, `curl`, `node`, `java`, `maven`, or `psql` is required — every verification step runs inside Docker containers.

Run the single Docker-contained verification command:

```bash
bash run_tests.sh
```

What this does, end-to-end, without any host language runtimes:

1. Builds the `pharmaprocure-test-tools` image (`API_tests/Dockerfile` — alpine + bash + curl + python3 + postgres client) used to drive API/smoke scripts.
2. Starts the full stack with `docker compose -f docker-compose.yml -f docker-compose.test.yml up -d --build` and waits for postgres, backend, and frontend to report healthy.
3. Runs the smoke suite inside the test-tools container (`docker run --rm --network pharmaprocure_default ... bash /workspace/scripts/smoke_test.sh`).
4. Runs backend unit tests inside a disposable Maven container (`docker run --rm -v $PWD/backend:/workspace maven:3.9.8-eclipse-temurin-17 mvn -q clean test`).
5. Runs every API suite inside the test-tools container on the compose network:
   - `bash /workspace/API_tests/auth_api_tests.sh`
   - `bash /workspace/API_tests/order_lifecycle_api_tests.sh`
   - `bash /workspace/API_tests/document_center_api_tests.sh`
   - `bash /workspace/API_tests/checkins_api_tests.sh`
   - `bash /workspace/API_tests/critical_actions_api_tests.sh`
   - `bash /workspace/API_tests/coverage_api_tests.sh`
6. Dumps diagnostic logs on failure and tears down all containers/volumes/networks on exit.

To run an individual API suite ad-hoc, invoke it **inside the test-tools container** — never on the host:

```bash
docker compose -f docker-compose.yml -f docker-compose.test.yml up -d --build
docker build -t pharmaprocure-test-tools -f API_tests/Dockerfile API_tests
docker run --rm --network pharmaprocure_default \
  -e BASE_URL=http://backend:8080 \
  -v "$PWD":/workspace -w /workspace \
  pharmaprocure-test-tools \
  bash /workspace/API_tests/auth_api_tests.sh
```

Health and version endpoints are likewise probed from inside the compose network rather than from the host:

```bash
docker run --rm --network pharmaprocure_default pharmaprocure-test-tools \
  sh -c 'curl -fsS http://backend:8080/api/health && curl -fsS http://backend:8080/api/meta/version'
```

Expected result: all suites pass, services become healthy, and the test summary reports zero failures. No `npm install`, `pip install`, `apt-get`, `mvn`, or manual database setup is required or supported — all tooling is containerized.

## Sample Test Users / Roles

> **Local development only.** The credentials below are seeded for demo and testing purposes on a
> single-machine Docker Compose stack. They are publicly known and **must never be used in any
> internet-facing or production deployment**. Rotate all passwords and review role assignments
> before exposing this application outside a trusted local network.

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
- `GET /api/orders/reason-codes?codeType=RETURN|AFTER_SALES`
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
- `PUT /api/admin/users/{id}`
- `PUT /api/admin/users/{id}/password` — admin password reset; enforces the password policy (≥ 12 chars, ≥ 3 character classes)
- `GET /api/admin/permissions`
- `GET /api/admin/state-machine`
- `PUT /api/admin/state-machine/{id}`
- `GET /api/admin/document-types`
- `PUT /api/admin/document-types/{id}`
- `GET /api/admin/reason-codes`
- `POST /api/admin/reason-codes`
- `PUT /api/admin/reason-codes/{id}`

## Test Command

Run strictly through bash + Docker — no host runtimes needed:

```bash
bash run_tests.sh
```

## Known Implementation Notes

- PostgreSQL is exposed on `5433` instead of `5432` to avoid local port conflicts observed during validation.
- Document preview watermarking is generated by the backend for preview content; direct downloads preserve the original signed artifact.
- Critical action expiration is enforced in backend service logic when requests are listed, retrieved, or acted upon.
