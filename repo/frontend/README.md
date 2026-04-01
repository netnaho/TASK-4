# Frontend

Angular frontend for the PharmaProcure compliance procurement portal.

## What It Depends On

- Backend API must be running for login and all business workflows.
- In local development the Angular app talks to `http://localhost:8080/api`.
- In the Docker deployment the frontend is served by Nginx on `http://localhost:4200` and proxies `/api` to the backend container.

## Recommended Startup

From the repository root:

```bash
docker compose up --build
```

Service URLs:

- App: `http://localhost:4200`
- Frontend health: `http://localhost:4200/health`
- Backend API health through frontend proxy: `http://localhost:4200/api/health`
- Backend API direct: `http://localhost:8080/api/health`

Seeded test users:

- Shared password: `PortalAccess2026!`
- Buyer: `buyer1`
- Fulfillment Clerk: `fulfillment1`
- Quality Reviewer: `quality1`
- Finance: `finance1`
- System Administrator: `admin1`

## Local Angular Development

Install dependencies:

```bash
npm ci
```

Run the Angular dev server:

```bash
npm start
```

The dev server runs at `http://localhost:4200` and expects the backend at `http://localhost:8080`.

## Build

Production build:

```bash
npm run build
```

Watch build:

```bash
npm run watch
```

## Test Commands

Unit/component tests:

```bash
npm run test:ci
```

Playwright end-to-end tests:

```bash
npm run e2e
```

Interactive Playwright runner:

```bash
npm run e2e:ui
```

## Frontend Verification Checklist

1. Open `http://localhost:4200/login`.
2. Sign in with any seeded account.
3. Verify role-specific navigation changes by account.
4. Check core workflows:
   - Buyer: orders, receipts, returns, check-ins, document drafts
   - Fulfillment: repeated partial shipments
   - Quality/Finance/Admin: approvals and governed actions

## Notes

- The production frontend uses `/api` and relies on the Nginx proxy in `frontend/docker/nginx.conf`.
- Playwright is already configured in this project; no extra e2e package setup is required.
