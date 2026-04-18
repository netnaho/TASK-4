# Delivery Acceptance + Project Architecture Audit (Static-Only)

Date: 2026-04-06  
Scope root: `repo/`  
Execution mode: **Static analysis only** (no runtime execution, no Docker, no test runs, no code modifications)

## 1. Verdict

**Overall conclusion: Partial Pass**

Reason: the delivery is broadly aligned with the Prompt and appears near-complete from a static standpoint, but at least one **High** security issue exists in abuse protection (spoofable login rate-limit identity via untrusted `X-Forwarded-For`) that materially weakens brute-force mitigation.

## 2. Scope and Static Verification Boundary

### What was reviewed

- Documentation and config: `repo/README.md`, `repo/backend/README.md`, `repo/docker-compose.yml`, `repo/docker-compose.test.yml`, `repo/run_tests.sh`
- Backend architecture and security: Spring entry/security/authz/services/repositories/migrations in `repo/backend/src/main/**`
- Frontend route/access and styling structure: `repo/frontend/src/main.ts`, `repo/frontend/src/app/app.config.ts`, `repo/frontend/src/app/app.routes.ts`, `repo/frontend/src/styles.scss`
- Tests (static only): integration/unit/security/e2e/API test scripts under `repo/backend/src/test/**`, `repo/frontend/e2e/**`, `repo/API_tests/**`

### What was not reviewed

- Runtime behavior under real network/browser/container conditions
- Actual DB migration execution outcomes
- CI/CD pipeline execution outputs in this audit session

### What was intentionally not executed

- No `docker compose`
- No backend/frontend startup
- No unit/integration/e2e/API test execution

### Claims requiring manual verification

- End-to-end runtime UX behavior, browser rendering fidelity, and interaction states under real devices
- Real-world deployment behavior with reverse proxy/load balancer trust configuration
- Operational characteristics under concurrent production load

## 3. Repository / Requirement Mapping Summary

- **Prompt business goal mapped:** offline-capable procurement portal with multi-role workflow, controlled documents, check-ins/evidence, compliance/audit, and security controls.
- **Mapped implementation areas:** Angular role-gated routes, Spring Security + method permissions + data-scope checks, order/document/check-in/critical-action services, Flyway schema for auditability, and multi-layer test assets.
- **Major constraints checked:** password policy, lockout/CAPTCHA, CSRF, headers/CSP, file-type/signature/size validation, SHA-256 + server-side signature, dual approval expiry, and static test/logging evidence.

## 4. Section-by-section Review

### 1) Hard Gates

#### 1.1 Documentation and static verifiability

- **Conclusion:** Pass
- **Rationale:** Startup/test/config guidance and entry points are present and mostly consistent with code structure.
- **Evidence:** `repo/README.md:16,32,45-49`; `repo/backend/README.md:31,38,47-48`; `repo/backend/src/main/java/com/pharmaprocure/portal/PharmaProcureApplication.java:6,10`; `repo/frontend/src/main.ts:1,5`; `repo/frontend/src/app/app.config.ts:24`

#### 1.2 Material deviation from Prompt

- **Conclusion:** Partial Pass
- **Rationale:** Core implementation aligns strongly; however, abuse-protection intent is weakened by spoofable login rate-limit identity (security deviation from “mitigate abuse” expectation).
- **Evidence:** `repo/backend/src/main/java/com/pharmaprocure/portal/security/RateLimitFilter.java:61-63,107-112`; `repo/backend/src/test/java/com/pharmaprocure/portal/security/RateLimitFilterTest.java:124-140`

---

### 2) Delivery Completeness

#### 2.1 Core explicit requirements coverage

- **Conclusion:** Partial Pass
- **Rationale:** Most core requirements have static implementation evidence (workflows, approvals, numbering, watermarking, validation, hashing/signing, RBAC/scope). One high-risk gap remains in rate-limit robustness.
- **Evidence:**
  - Role routes: `repo/frontend/src/app/app.routes.ts:12,16-26`
  - Security chain + CSRF + headers: `repo/backend/src/main/java/com/pharmaprocure/portal/security/SecurityConfig.java:57,64-67,73-77`
  - Password policy: `repo/backend/src/main/java/com/pharmaprocure/portal/service/PasswordPolicyService.java:25,43`
  - Lockout/CAPTCHA: `repo/backend/src/main/java/com/pharmaprocure/portal/service/LoginAttemptPolicyService.java:11-13,30,36-37`; `repo/backend/src/main/java/com/pharmaprocure/portal/service/CaptchaService.java:50-64`
  - Document numbering format/reset basis: `repo/backend/src/main/java/com/pharmaprocure/portal/service/DocumentNumberingService.java:30-35`; `repo/backend/src/main/resources/db/migration/V4__document_center.sql:91-96`
  - Dual approval and expiry: `repo/backend/src/main/java/com/pharmaprocure/portal/service/CriticalActionService.java:121,141,145,279-281`

#### 2.2 0→1 end-to-end deliverable vs partial demo

- **Conclusion:** Pass
- **Rationale:** Full-stack structure, migration-backed persistence, API surface, frontend app, and multi-layer tests are present.
- **Evidence:** `repo/backend/pom.xml:1-21,23-88`; `repo/frontend/package.json:1-42`; `repo/backend/src/main/resources/db/migration/V4__document_center.sql:27,41,78,98`; `repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:50,83,102`; `repo/API_tests/README.md:3-8`

---

### 3) Engineering and Architecture Quality

#### 3.1 Structure and module decomposition

- **Conclusion:** Pass
- **Rationale:** Clear separation across controllers/services/repositories/security/audit/migrations and frontend features/guards/routes.
- **Evidence:** `repo/backend/src/main/java/com/pharmaprocure/portal/controller/AdminController.java:29`; `repo/backend/src/main/java/com/pharmaprocure/portal/security/SecurityConfig.java:25`; `repo/backend/src/main/java/com/pharmaprocure/portal/repository/ProcurementOrderRepository.java:13`; `repo/frontend/src/app/app.routes.ts:1-4,16-26`

#### 3.2 Maintainability/extensibility

- **Conclusion:** Pass
- **Rationale:** Uses service-level business rules, repository filtering, and permission abstractions rather than single-file hardcoding.
- **Evidence:** `repo/backend/src/main/java/com/pharmaprocure/portal/security/PermissionAuthorizationService.java`; `repo/backend/src/main/java/com/pharmaprocure/portal/repository/DocumentRepository.java:31-40`; `repo/backend/src/main/java/com/pharmaprocure/portal/service/DocumentNumberingService.java:25-48`

---

### 4) Engineering Details and Professionalism

#### 4.1 Error handling/logging/validation/API quality

- **Conclusion:** Partial Pass
- **Rationale:** Strong global exception handling, validation, and log masking/sanitization are present; however, rate-limit identity trust issue is a material security-engineering defect.
- **Evidence:** `repo/backend/src/main/java/com/pharmaprocure/portal/exception/GlobalExceptionHandler.java:29-33,50-53,63`; `repo/backend/src/main/java/com/pharmaprocure/portal/util/MaskingUtils.java:14,23,29`; `repo/backend/src/main/java/com/pharmaprocure/portal/audit/AuditService.java:22-30`; `repo/backend/src/main/java/com/pharmaprocure/portal/security/RateLimitFilter.java:108-110`

#### 4.2 Product-like deliverable vs demo-only

- **Conclusion:** Pass
- **Rationale:** Includes realistic domain entities, approvals, audit, migrations, and structured tests.
- **Evidence:** `repo/backend/src/main/resources/db/migration/V4__document_center.sql:27-109`; `repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:27,84`; `repo/frontend/e2e/cross-role-order-workflow.spec.ts:42-89`

---

### 5) Prompt Understanding and Requirement Fit

#### 5.1 Business goal and semantic fit

- **Conclusion:** Partial Pass
- **Rationale:** Procurement/document/evidence/compliance workflows are implemented with strong alignment; abuse-mitigation semantics are partially weakened by spoofable login-IP identity.
- **Evidence:** `repo/frontend/src/app/app.routes.ts:16-26`; `repo/backend/src/main/java/com/pharmaprocure/portal/service/CriticalActionService.java:121,141,145`; `repo/backend/src/main/java/com/pharmaprocure/portal/security/RateLimitFilter.java:61-63,108-110`

---

### 6) Aesthetics (frontend-only/full-stack)

#### 6.1 Visual/interaction design quality

- **Conclusion:** Cannot Confirm Statistically
- **Rationale:** Styling system and interaction hooks exist statically, but visual correctness/usability requires runtime browser verification.
- **Evidence:** `repo/frontend/src/styles.scss:1,31,40-57,103-118,247`; `repo/frontend/src/app/app.routes.ts:16-26`
- **Manual verification required:** cross-browser rendering, responsive layout behavior, and interaction feel.

## 5. Issues / Suggestions (Severity-Rated)

### High

1. **Spoofable login rate-limit identity via untrusted `X-Forwarded-For`**

- **Severity:** High
- **Conclusion:** Fail for this control
- **Evidence:**
  - Login limiter keys by `clientIp`: `repo/backend/src/main/java/com/pharmaprocure/portal/security/RateLimitFilter.java:61-63`
  - Client IP resolution trusts user-supplied header directly: `repo/backend/src/main/java/com/pharmaprocure/portal/security/RateLimitFilter.java:107-110`
  - Test codifies header-based identity behavior: `repo/backend/src/test/java/com/pharmaprocure/portal/security/RateLimitFilterTest.java:124-140`
- **Impact:** Attackers can rotate spoofed header values to evade per-IP login throttling, materially reducing brute-force resistance.
- **Minimum actionable fix:** Only honor forwarding headers when request originates from trusted proxy CIDRs (or use framework forward-header strategy with trusted boundary); otherwise use `remoteAddr`. Add explicit anti-spoof tests.

### Medium

2. **CSRF and CAPTCHA endpoints excluded from rate limiting (abuse surface)**

- **Severity:** Medium
- **Conclusion:** Partial Pass
- **Evidence:**
  - Excluded paths include `/api/auth/csrf` and `/api/auth/captcha`: `repo/backend/src/main/java/com/pharmaprocure/portal/security/RateLimitFilter.java:51-54`
  - Tests assert unlimited pass-through: `repo/backend/src/test/java/com/pharmaprocure/portal/security/RateLimitFilterTest.java:98-106`
- **Impact:** Potential endpoint abuse/amplification (especially CAPTCHA issuance) and weaker abuse-mitigation posture.
- **Minimum actionable fix:** Add bounded IP-based throttles for these endpoints (with higher limits than login), plus tests for threshold and backoff behavior.

3. **No static test evidence for sensitive-data masking regression prevention in logs/responses**

- **Severity:** Medium
- **Conclusion:** Partial Pass
- **Evidence:**
  - Masking/sanitization exists in implementation: `repo/backend/src/main/java/com/pharmaprocure/portal/util/MaskingUtils.java:14,23-29`; `repo/backend/src/main/java/com/pharmaprocure/portal/audit/AuditService.java:22-30`; `repo/backend/src/main/java/com/pharmaprocure/portal/exception/GlobalExceptionHandler.java:29-33`
  - No dedicated masking-focused test surfaced in current test set index: `repo/backend/src/test/java/**/*Test.java` (56 files; none named for masking/audit sanitization)
- **Impact:** Future regressions could reintroduce data leakage without detection.
- **Minimum actionable fix:** Add unit/integration tests asserting masked principal output and sanitized exception/audit details for representative PII-like inputs.

### Low

4. **Shared credential string documented in root README**

- **Severity:** Low
- **Conclusion:** Advisory
- **Evidence:** `repo/README.md:62`
- **Impact:** Increases accidental reuse risk if copied into non-local environments.
- **Minimum actionable fix:** Mark as local-demo-only, rotate by default, and add explicit production warning.

## 6. Security Review Summary

- **Authentication entry points — Pass**  
  Evidence: `repo/backend/src/main/java/com/pharmaprocure/portal/controller/AuthController.java:22,33,44,49,54`; `repo/backend/src/main/java/com/pharmaprocure/portal/service/AuthService.java:54,59,67,89,95`

- **Route-level authorization — Pass**  
  Evidence: `repo/backend/src/main/java/com/pharmaprocure/portal/security/SecurityConfig.java:73-77`; frontend role gating `repo/frontend/src/app/app.routes.ts:12,16-26`

- **Object-level authorization — Pass**  
  Evidence: scope checks in services/repositories: `repo/backend/src/main/java/com/pharmaprocure/portal/service/CheckInService.java:187-195`; `repo/backend/src/main/java/com/pharmaprocure/portal/service/CriticalActionService.java:251-274`; `repo/backend/src/main/java/com/pharmaprocure/portal/repository/DocumentRepository.java:31-40`

- **Function-level authorization — Pass**  
  Evidence: method-level annotations and permission service: `repo/backend/src/main/java/com/pharmaprocure/portal/controller/AdminController.java:30`; `repo/backend/src/main/java/com/pharmaprocure/portal/controller/DocumentCenterController.java:43,55,70,83,89,101,107,114`

- **Tenant/user data isolation — Pass**  
  Evidence: filtered query patterns and scope-restriction assertions: `repo/backend/src/main/java/com/pharmaprocure/portal/repository/ProcurementOrderRepository.java:31-40`; `repo/backend/src/main/java/com/pharmaprocure/portal/repository/CheckInRepository.java:29-36`; `repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:31`; `repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:86`

- **Admin/internal/debug protection — Partial Pass**  
  Evidence: admin route restricted `repo/backend/src/main/java/com/pharmaprocure/portal/security/SecurityConfig.java:73`; admin class preauthorize `repo/backend/src/main/java/com/pharmaprocure/portal/controller/AdminController.java:30`; but abuse control around login identity remains weak (`RateLimitFilter.java:108-110`).

## 7. Tests and Logging Review

- **Unit tests — Pass (static existence and breadth)**  
  Evidence: 56 test files including service/security units (e.g., `repo/backend/src/test/java/com/pharmaprocure/portal/service/PasswordPolicyServiceTest.java:8`, `.../DocumentNumberingServiceTest.java:17`, `.../security/RateLimitFilterTest.java:13`)

- **API / integration tests — Pass (static existence and coverage breadth)**  
  Evidence: `repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java`; `.../CriticalActionControllerIntegrationTest.java`; `.../NegativePathIntegrationTest.java:20,54`; `.../SecurityHeadersIntegrationTest.java:16-19`; e2e specs `repo/frontend/e2e/*.spec.ts`

- **Logging categories / observability — Partial Pass**  
  Evidence: centralized audit logging exists (`repo/backend/src/main/java/com/pharmaprocure/portal/audit/AuditService.java:14,23,30`), but masking regression tests are missing (see Issue #3).

- **Sensitive-data leakage risk in logs / responses — Partial Pass**  
  Evidence: masking/sanitization implemented (`MaskingUtils.java:14,23-29`; `GlobalExceptionHandler.java:29-33`), but no dedicated automated verification detected.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview

- **Unit tests exist:** Yes (`repo/backend/src/test/java/com/pharmaprocure/portal/service/*Test.java`, `repo/backend/src/test/java/com/pharmaprocure/portal/security/*Test.java`)
- **API/integration tests exist:** Yes (`repo/backend/src/test/java/com/pharmaprocure/portal/integration/*IntegrationTest.java`)
- **Frontend e2e exists:** Yes (`repo/frontend/e2e/cross-role-order-workflow.spec.ts`, `repo/frontend/e2e/document-and-checkin-flow.spec.ts`)
- **Frameworks (static evidence):** JUnit/Spring Test in backend (`repo/backend/pom.xml:69-84`), Playwright in frontend (`repo/frontend/package.json:33`)
- **Test entry points documented:** Yes (`repo/README.md:45-49`; `repo/backend/README.md:95-96`; `repo/run_tests.sh:30-36`)

### 8.2 Coverage Mapping Table

| Requirement / Risk Point                              | Mapped Test Case(s)                                                                                                                                                                 | Key Assertion / Fixture / Mock                                      | Coverage Assessment | Gap                                                       | Minimum Test Addition                                                                  |
| ----------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------- | ------------------- | --------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| 401 unauthenticated and 404 missing-resource handling | `NegativePathIntegrationTest.java:20,26,32,38,44,54,62,70,78`                                                                                                                       | Explicit `isUnauthorized()` and `isNotFound()` expectations         | sufficient          | None material in static scope                             | Keep as baseline regression suite                                                      |
| Security headers/CSP                                  | `SecurityHeadersIntegrationTest.java:16-19,28-31,37`                                                                                                                                | Header exact-value assertions for nosniff/frame/CSP/referrer policy | sufficient          | Runtime browser CSP behavior not proven                   | Add browser-level CSP violation e2e check                                              |
| CSRF enforcement on login                             | `AuthControllerIntegrationTest.java:17,23,31`                                                                                                                                       | Missing CSRF forbidden + with CSRF path                             | basically covered   | Not exhaustive across all mutating endpoints              | Add representative mutating endpoint CSRF matrix                                       |
| Role/scope authorization boundaries                   | `OrderControllerIntegrationTest.java:31`; `CheckInControllerIntegrationTest.java:86`; `AdminControllerIntegrationTest.java:107`; `CriticalActionControllerIntegrationTest.java:266` | Scope restriction and forbidden assertions                          | basically covered   | Could expand cross-org matrix breadth                     | Add parametric cross-org/cross-role matrix tests                                       |
| Rate limiting (threshold behavior)                    | `RateLimitIntegrationTest.java:47`; `RateLimitFilterTest.java:35-46,66-79`                                                                                                          | 20/min login and 60/min authenticated limits                        | basically covered   | Spoof-resistance not validated                            | Add tests for untrusted `X-Forwarded-For` rejection and trusted-proxy-only behavior    |
| Dual approval and expiry semantics                    | `CriticalActionControllerIntegrationTest.java:27,84`; service rules `CriticalActionService.java:121,141,145,279-281`                                                                | Expiry-on-read and cross-role rejection assertion                   | basically covered   | More negative combinations possible                       | Add matrix for lane permutations and replay attempts                                   |
| Document numbering format/year reset                  | `DocumentNumberingServiceTest.java:31,44,59`                                                                                                                                        | Asserts `TYPE-YYYY-000001` formatting and year-specific sequence    | sufficient          | Runtime timezone boundary not confirmed                   | Add boundary test around year rollover clock instant                                   |
| Watermarked preview behavior                          | `DocumentCenterControllerIntegrationTest.java:32,53`                                                                                                                                | Asserts preview bytes changed by watermark pipeline                 | basically covered   | Watermark content fields not fully asserted textually     | Add parsing/assertions for username/timestamp/doc number tokens                        |
| File upload validation (type/signature/size)          | `DocumentFileValidationService.java:16,24-33,36-42` + integration bad-request checks (`DocumentCenterControllerIntegrationTest.java:91`)                                            | MIME/signature logic and error-path checks                          | basically covered   | No explicit integration assertion for 25MB boundary shown | Add max-size boundary and WAV signature edge cases                                     |
| Masking/sanitization leakage prevention               | impl only (`MaskingUtils.java:14,23-29`; `AuditService.java:22-30`)                                                                                                                 | N/A                                                                 | insufficient        | No dedicated automated tests                              | Add unit tests for masking/sanitize patterns + integration log/message contract checks |

### 8.3 Security Coverage Audit

- **Authentication:** basically covered (login/CSRF/lockout paths present), but spoof-resistant login throttling not covered.
- **Route authorization:** basically covered with forbidden tests and route restrictions.
- **Object-level authorization:** basically covered via scope-restriction assertions in multiple modules.
- **Tenant/data isolation:** basically covered via scoped repository filters and selected tests; broader matrix still advisable.
- **Admin/internal protection:** basically covered for role guard; abuse control weakness around login rate-limit identity remains.

### 8.4 Final Coverage Judgment

**Partial Pass**

- **Major risks covered:** authentication/authorization negative paths, headers/CSP, core workflow statuses, dual-approval semantics, numbering format.
- **Uncovered/under-covered risks that could still permit severe defects:** spoof-resistant login throttling behavior, masking regression tests, and exhaustive cross-tenant authorization matrix.

## 9. Final Notes

- This report is strictly static and does not claim runtime success.
- Findings are root-cause focused and evidence-traceable.
- Highest priority remediation is hardening trusted client-IP derivation for login abuse controls.
