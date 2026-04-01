# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PharmaProcure is a compliance procurement portal for regulated pharmaceutical operations. It runs entirely via Docker Compose with three services: Angular 17 frontend, Spring Boot 3 backend (Java 17), and PostgreSQL 16.

## Commands

### Start the full stack
```bash
docker compose up --build
```
Services: frontend at :4200, backend API at :8080, PostgreSQL at :5433.

### Run all tests (requires running containers)
```bash
./run_tests.sh
```
This rebuilds containers, waits for health, runs smoke tests, backend unit tests, and all API test suites.

### Backend unit tests only (no running containers needed)
```bash
docker run --rm -v "$PWD/backend":/workspace -w /workspace maven:3.9.8-eclipse-temurin-17 mvn -q clean test
```

### Run a single backend test class
```bash
docker run --rm -v "$PWD/backend":/workspace -w /workspace maven:3.9.8-eclipse-temurin-17 mvn -q test -Dtest=OrderStateMachineServiceTest
```

### Individual API test suites (requires running stack)
```bash
bash ./API_tests/auth_api_tests.sh
bash ./API_tests/order_lifecycle_api_tests.sh
bash ./API_tests/document_center_api_tests.sh
bash ./API_tests/checkins_api_tests.sh
bash ./API_tests/critical_actions_api_tests.sh
```

### Frontend (local dev, from frontend/)
```bash
npm start          # ng serve on :4200
npm test           # Karma/Jasmine unit tests
npm run test:ci    # headless Chrome
npm run e2e        # Playwright
```

### Backend build (from backend/)
```bash
mvn clean package
```

## Architecture

### Backend (`backend/`)
Spring Boot 3 with layered package structure under `com.pharmaprocure.portal`:
- **controller** — REST endpoints (auth, orders, documents, check-ins, critical-actions, admin)
- **service** — business logic including order state machine, document workflows, dual approval
- **repository** — Spring Data JPA repositories
- **entity** / **dto** / **mapper** — domain objects, API contracts, MapStruct mappers
- **security** — session-based auth, RBAC, CSRF, CAPTCHA, login lockout, rate limiting
- **validation** — custom validators
- **audit** — audit event logging
- **config** / **util** / **enums** / **exception** — cross-cutting concerns

Database schema is managed by Flyway migrations in `backend/src/main/resources/db/migration/` (V1–V9). DDL-auto is set to `validate` — all schema changes must go through new migration files.

Backend tests use H2 in-memory database. Test packages: `service` (unit), `security` (unit), `integration` (MockMvc-based, extending `AbstractMockMvcIntegrationTest`).

Uses Lombok for boilerplate and MapStruct for entity/DTO mapping (both configured as annotation processors in pom.xml).

### Frontend (`frontend/`)
Angular 17 with standalone components (no NgModules). Structure:
- **core/** — guards (`auth`, `role`), interceptors, services (one per backend domain), models (typed interfaces)
- **features/** — lazy-loaded pages: admin, approvals, auth, check-ins, dashboard, documents, orders
- **layout/** — shell component wrapping authenticated routes
- **shared/** — reusable UI components

All routes use `loadComponent` lazy loading. Role-based access is enforced via `roleGuard` with `data: { roles: [...] }` on each route.

Angular Material with custom theme and offline SVG icons (no CDN dependencies).

### Data & Auth
- Five roles: BUYER, FULFILLMENT_CLERK, QUALITY_REVIEWER, FINANCE, SYSTEM_ADMINISTRATOR
- Session-based authentication (not JWT)
- Dual approval workflow for critical actions (order cancellation, document destruction) — same user cannot provide both approvals
- Documents have SHA-256 hashing and server-side signature metadata
- Test credentials: any seeded user (buyer1, fulfillment1, quality1, finance1, admin1, quality2, finance2) with password `PortalAccess2026!`

## Key Design Decisions

- PostgreSQL exposed on port **5433** (not 5432) to avoid local conflicts
- Document/evidence files stored on a Docker volume at `/app/data/documents` inside the backend container
- Critical action expiration (24h) is enforced in backend service logic at read/write time, not via scheduled jobs
- Document preview watermarking is a visible overlay, not binary PDF rewriting
- Hibernate `open-in-view: false` — lazy loading must be handled explicitly in services
