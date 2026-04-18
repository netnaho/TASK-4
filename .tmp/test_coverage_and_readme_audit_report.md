# Test Coverage Audit

## Project Type Detection
- README top does **not** explicitly declare one of: `backend|fullstack|web|android|ios|desktop` ([repo/README.md:1](repo/README.md:1)).
- Inferred type (light inspection): **fullstack** (Angular frontend + Spring backend + Docker Compose) from [repo/README.md:3](repo/README.md:3), [repo/frontend/angular.json:7](repo/frontend/angular.json:7), [repo/backend/src/main/java/com/pharmaprocure/portal/controller/OrderController.java:31](repo/backend/src/main/java/com/pharmaprocure/portal/controller/OrderController.java:31).

## Backend Endpoint Inventory
1. `GET /api/catalog/products`
2. `GET /api/health`
3. `GET /api/meta/version`
4. `GET /api/orders/workspace`
5. `GET /api/documents/review`
6. `GET /api/admin/panel`
7. `GET /api/auth/csrf`
8. `GET /api/auth/captcha`
9. `POST /api/auth/login`
10. `POST /api/auth/logout`
11. `GET /api/auth/me`
12. `GET /api/check-ins`
13. `POST /api/check-ins`
14. `PUT /api/check-ins/{checkInId}`
15. `GET /api/check-ins/{checkInId}`
16. `GET /api/check-ins/{checkInId}/attachments/{attachmentId}/download`
17. `GET /api/documents/types`
18. `GET /api/documents/templates`
19. `POST /api/documents/templates`
20. `GET /api/documents`
21. `GET /api/documents/approval-queue`
22. `GET /api/documents/archive`
23. `POST /api/documents`
24. `PUT /api/documents/{documentId}`
25. `GET /api/documents/{documentId}`
26. `POST /api/documents/{documentId}/submit-approval`
27. `POST /api/documents/{documentId}/approve`
28. `POST /api/documents/{documentId}/archive`
29. `GET /api/documents/{documentId}/preview`
30. `GET /api/documents/{documentId}/content`
31. `GET /api/documents/{documentId}/download`
32. `GET /api/admin/users`
33. `PUT /api/admin/users/{id}`
34. `PUT /api/admin/users/{id}/password`
35. `GET /api/admin/permissions`
36. `GET /api/admin/state-machine`
37. `PUT /api/admin/state-machine/{id}`
38. `GET /api/admin/document-types`
39. `PUT /api/admin/document-types/{id}`
40. `GET /api/admin/reason-codes`
41. `POST /api/admin/reason-codes`
42. `PUT /api/admin/reason-codes/{id}`
43. `GET /api/critical-actions`
44. `GET /api/critical-actions/{requestId}`
45. `POST /api/critical-actions`
46. `POST /api/critical-actions/{requestId}/decision`
47. `GET /api/orders`
48. `GET /api/orders/{orderId}`
49. `GET /api/orders/reason-codes`
50. `POST /api/orders`
51. `POST /api/orders/{orderId}/submit-review`
52. `POST /api/orders/{orderId}/cancel`
53. `POST /api/orders/{orderId}/approve`
54. `POST /api/orders/{orderId}/record-payment`
55. `POST /api/orders/{orderId}/pick-pack`
56. `POST /api/orders/{orderId}/shipments`
57. `POST /api/orders/{orderId}/receipts`
58. `POST /api/orders/{orderId}/returns`
59. `POST /api/orders/{orderId}/after-sales-cases`
60. `GET /api/orders/{orderId}/traceability`

Endpoint sources: controller mappings in [repo/backend/src/main/java/com/pharmaprocure/portal/controller](repo/backend/src/main/java/com/pharmaprocure/portal/controller).

## API Test Mapping Table
| Endpoint | Covered | Test Type | Test Files | Evidence |
|---|---|---|---|---|
| GET /api/catalog/products | no | unit-only / indirect | - | - |
| GET /api/health | yes | true no-mock HTTP | SecurityHeadersIntegrationTest | `publicEndpointCarriesSecurityHeaders` [repo/backend/src/test/java/com/pharmaprocure/portal/integration/SecurityHeadersIntegrationTest.java:14](repo/backend/src/test/java/com/pharmaprocure/portal/integration/SecurityHeadersIntegrationTest.java:14) |
| GET /api/meta/version | no | unit-only / indirect | - | - |
| GET /api/orders/workspace | yes | true no-mock HTTP | auth_api_tests.sh | buyer workspace request [repo/API_tests/auth_api_tests.sh:95](repo/API_tests/auth_api_tests.sh:95) |
| GET /api/documents/review | no | unit-only / indirect | - | - |
| GET /api/admin/panel | yes | true no-mock HTTP | auth_api_tests.sh | admin panel access checks [repo/API_tests/auth_api_tests.sh:85](repo/API_tests/auth_api_tests.sh:85), [repo/API_tests/auth_api_tests.sh:103](repo/API_tests/auth_api_tests.sh:103) |
| GET /api/auth/csrf | yes | true no-mock HTTP | API test scripts, Playwright e2e | csrf fetch [repo/API_tests/auth_api_tests.sh:23](repo/API_tests/auth_api_tests.sh:23), [repo/frontend/e2e/cross-role-order-workflow.spec.ts:12](repo/frontend/e2e/cross-role-order-workflow.spec.ts:12) |
| GET /api/auth/captcha | yes | true no-mock HTTP | auth_api_tests.sh | captcha retrieval [repo/API_tests/auth_api_tests.sh:123](repo/API_tests/auth_api_tests.sh:123) |
| POST /api/auth/login | yes | true no-mock HTTP | AuthControllerIntegrationTest, SanitizationIntegrationTest, RateLimitIntegrationTest, auth_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AuthControllerIntegrationTest.java:20](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AuthControllerIntegrationTest.java:20), [repo/API_tests/auth_api_tests.sh:88](repo/API_tests/auth_api_tests.sh:88) |
| POST /api/auth/logout | yes | true no-mock HTTP | auth_api_tests.sh, Playwright e2e | [repo/API_tests/auth_api_tests.sh:162](repo/API_tests/auth_api_tests.sh:162), [repo/frontend/e2e/document-and-checkin-flow.spec.ts:15](repo/frontend/e2e/document-and-checkin-flow.spec.ts:15) |
| GET /api/auth/me | yes | true no-mock HTTP | auth_api_tests.sh | [repo/API_tests/auth_api_tests.sh:92](repo/API_tests/auth_api_tests.sh:92) |
| GET /api/check-ins | yes | true no-mock HTTP | NegativePathIntegrationTest | unauthenticated access check [repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:31](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:31) |
| POST /api/check-ins | yes | true no-mock HTTP | CheckInControllerIntegrationTest, checkins_api_tests.sh | multipart create [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:117](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:117), [repo/API_tests/checkins_api_tests.sh:60](repo/API_tests/checkins_api_tests.sh:60) |
| PUT /api/check-ins/{checkInId} | yes | true no-mock HTTP | CheckInControllerIntegrationTest, checkins_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:60](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:60), [repo/API_tests/checkins_api_tests.sh:77](repo/API_tests/checkins_api_tests.sh:77) |
| GET /api/check-ins/{checkInId} | yes | true no-mock HTTP | CheckInControllerIntegrationTest, checkins_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:84](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:84), [repo/API_tests/checkins_api_tests.sh:81](repo/API_tests/checkins_api_tests.sh:81) |
| GET /api/check-ins/{checkInId}/attachments/{attachmentId}/download | yes | true no-mock HTTP | checkins_api_tests.sh | [repo/API_tests/checkins_api_tests.sh:90](repo/API_tests/checkins_api_tests.sh:90) |
| GET /api/documents/types | no | unit-only / indirect | - | - |
| GET /api/documents/templates | no | unit-only / indirect | - | - |
| POST /api/documents/templates | yes | true no-mock HTTP | document_center_api_tests.sh | [repo/API_tests/document_center_api_tests.sh:76](repo/API_tests/document_center_api_tests.sh:76) |
| GET /api/documents | yes | true no-mock HTTP | NegativePathIntegrationTest | unauthenticated access check [repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:25](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:25) |
| GET /api/documents/approval-queue | no | unit-only / indirect | - | - |
| GET /api/documents/archive | no | unit-only / indirect | - | - |
| POST /api/documents | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest, document_center_api_tests.sh, critical_actions_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:87](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:87), [repo/API_tests/document_center_api_tests.sh:79](repo/API_tests/document_center_api_tests.sh:79) |
| PUT /api/documents/{documentId} | no | unit-only / indirect | - | - |
| GET /api/documents/{documentId} | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest, NegativePathIntegrationTest, document_center_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:65](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:65), [repo/API_tests/document_center_api_tests.sh:114](repo/API_tests/document_center_api_tests.sh:114) |
| POST /api/documents/{documentId}/submit-approval | yes | true no-mock HTTP | document_center_api_tests.sh, critical_actions_api_tests.sh | [repo/API_tests/document_center_api_tests.sh:93](repo/API_tests/document_center_api_tests.sh:93) |
| POST /api/documents/{documentId}/approve | yes | true no-mock HTTP | document_center_api_tests.sh, critical_actions_api_tests.sh | [repo/API_tests/document_center_api_tests.sh:100](repo/API_tests/document_center_api_tests.sh:100) |
| POST /api/documents/{documentId}/archive | yes | true no-mock HTTP | document_center_api_tests.sh, critical_actions_api_tests.sh | [repo/API_tests/document_center_api_tests.sh:103](repo/API_tests/document_center_api_tests.sh:103) |
| GET /api/documents/{documentId}/preview | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest, document_center_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:42](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:42), [repo/API_tests/document_center_api_tests.sh:107](repo/API_tests/document_center_api_tests.sh:107) |
| GET /api/documents/{documentId}/content | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:46](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:46) |
| GET /api/documents/{documentId}/download | yes | true no-mock HTTP | document_center_api_tests.sh | [repo/API_tests/document_center_api_tests.sh:111](repo/API_tests/document_center_api_tests.sh:111) |
| GET /api/admin/users | yes | true no-mock HTTP | NegativePathIntegrationTest | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:43](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:43) |
| PUT /api/admin/users/{id} | yes | true no-mock HTTP | AdminControllerIntegrationTest | `adminCanSuspendUserAccess` [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:44](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:44) |
| PUT /api/admin/users/{id}/password | yes | true no-mock HTTP | AdminControllerIntegrationTest | `adminCanResetUserPasswordWithValidPolicy` [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:59](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:59) |
| GET /api/admin/permissions | no | unit-only / indirect | - | - |
| GET /api/admin/state-machine | yes | true no-mock HTTP | AdminControllerIntegrationTest | `adminCanUpdateStateMachineTransition` [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:34](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:34) |
| PUT /api/admin/state-machine/{id} | yes | true no-mock HTTP | AdminControllerIntegrationTest | `adminCanUpdateStateMachineTransition` [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:25](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:25) |
| GET /api/admin/document-types | no | unit-only / indirect | - | - |
| PUT /api/admin/document-types/{id} | no | unit-only / indirect | - | - |
| GET /api/admin/reason-codes | no | unit-only / indirect | - | - |
| POST /api/admin/reason-codes | no | unit-only / indirect | - | - |
| PUT /api/admin/reason-codes/{id} | yes | true no-mock HTTP | OrderControllerIntegrationTest | `returnsAndAfterSalesRequireActiveManagedReasonCodes` [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:156](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:156) |
| GET /api/critical-actions | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, NegativePathIntegrationTest | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:43](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:43) |
| GET /api/critical-actions/{requestId} | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, NegativePathIntegrationTest, critical_actions_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:265](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:265), [repo/API_tests/critical_actions_api_tests.sh:100](repo/API_tests/critical_actions_api_tests.sh:100) |
| POST /api/critical-actions | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, critical_actions_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:143](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:143), [repo/API_tests/critical_actions_api_tests.sh:41](repo/API_tests/critical_actions_api_tests.sh:41) |
| POST /api/critical-actions/{requestId}/decision | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, IdempotencyIntegrationTest, ConcurrencyIntegrationTest, critical_actions_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:70](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:70), [repo/API_tests/critical_actions_api_tests.sh:47](repo/API_tests/critical_actions_api_tests.sh:47) |
| GET /api/orders | yes | true no-mock HTTP | PaginationIntegrationTest, NegativePathIntegrationTest, SecurityHeadersIntegrationTest, RateLimitIntegrationTest | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/PaginationIntegrationTest.java:20](repo/backend/src/test/java/com/pharmaprocure/portal/integration/PaginationIntegrationTest.java:20) |
| GET /api/orders/{orderId} | yes | true no-mock HTTP | OrderControllerIntegrationTest, NegativePathIntegrationTest, CriticalActionControllerIntegrationTest, API scripts | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:29](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:29), [repo/API_tests/order_lifecycle_api_tests.sh:140](repo/API_tests/order_lifecycle_api_tests.sh:140) |
| GET /api/orders/reason-codes | yes | true no-mock HTTP | OrderControllerIntegrationTest | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:194](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:194) |
| POST /api/orders | yes | true no-mock HTTP | SanitizationIntegrationTest, order_lifecycle_api_tests.sh, critical_actions_api_tests.sh, Playwright e2e | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/SanitizationIntegrationTest.java:64](repo/backend/src/test/java/com/pharmaprocure/portal/integration/SanitizationIntegrationTest.java:64), [repo/frontend/e2e/cross-role-order-workflow.spec.ts:45](repo/frontend/e2e/cross-role-order-workflow.spec.ts:45) |
| POST /api/orders/{orderId}/submit-review | yes | true no-mock HTTP | IdempotencyIntegrationTest, ConcurrencyIntegrationTest, order_lifecycle_api_tests.sh, Playwright e2e | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:31](repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:31), [repo/frontend/e2e/cross-role-order-workflow.spec.ts:48](repo/frontend/e2e/cross-role-order-workflow.spec.ts:48) |
| POST /api/orders/{orderId}/cancel | no | unit-only / indirect | - | No direct POST to this route found in integration/API/e2e test files |
| POST /api/orders/{orderId}/approve | yes | true no-mock HTTP | OrderControllerIntegrationTest, IdempotencyIntegrationTest, order_lifecycle_api_tests.sh, critical_actions_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:205](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:205), [repo/API_tests/order_lifecycle_api_tests.sh:82](repo/API_tests/order_lifecycle_api_tests.sh:82) |
| POST /api/orders/{orderId}/record-payment | yes | true no-mock HTTP | IdempotencyIntegrationTest, order_lifecycle_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:117](repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:117), [repo/API_tests/order_lifecycle_api_tests.sh:88](repo/API_tests/order_lifecycle_api_tests.sh:88) |
| POST /api/orders/{orderId}/pick-pack | yes | true no-mock HTTP | IdempotencyIntegrationTest, order_lifecycle_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:140](repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:140), [repo/API_tests/order_lifecycle_api_tests.sh:94](repo/API_tests/order_lifecycle_api_tests.sh:94) |
| POST /api/orders/{orderId}/shipments | yes | true no-mock HTTP | OrderControllerIntegrationTest, order_lifecycle_api_tests.sh, Playwright e2e | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:41](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:41), [repo/frontend/e2e/cross-role-order-workflow.spec.ts:66](repo/frontend/e2e/cross-role-order-workflow.spec.ts:66) |
| POST /api/orders/{orderId}/receipts | yes | true no-mock HTTP | OrderControllerIntegrationTest, order_lifecycle_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:54](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:54), [repo/API_tests/order_lifecycle_api_tests.sh:111](repo/API_tests/order_lifecycle_api_tests.sh:111) |
| POST /api/orders/{orderId}/returns | yes | true no-mock HTTP | OrderControllerIntegrationTest, order_lifecycle_api_tests.sh | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:170](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:170), [repo/API_tests/order_lifecycle_api_tests.sh:120](repo/API_tests/order_lifecycle_api_tests.sh:120) |
| POST /api/orders/{orderId}/after-sales-cases | yes | true no-mock HTTP | OrderControllerIntegrationTest | [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:182](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:182) |
| GET /api/orders/{orderId}/traceability | yes | true no-mock HTTP | order_lifecycle_api_tests.sh | [repo/API_tests/order_lifecycle_api_tests.sh:123](repo/API_tests/order_lifecycle_api_tests.sh:123) |

## API Test Classification
### 1) True No-Mock HTTP
- Backend MockMvc integration tests boot full Spring context (`@SpringBootTest`, `@AutoConfigureMockMvc`) with real routing and no `@MockBean` overrides in integration package: [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AbstractMockMvcIntegrationTest.java:35](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AbstractMockMvcIntegrationTest.java:35).
- Shell API suites using `curl` against live HTTP endpoints: [repo/API_tests/auth_api_tests.sh:40](repo/API_tests/auth_api_tests.sh:40), [repo/API_tests/order_lifecycle_api_tests.sh:32](repo/API_tests/order_lifecycle_api_tests.sh:32), [repo/API_tests/document_center_api_tests.sh:26](repo/API_tests/document_center_api_tests.sh:26), [repo/API_tests/checkins_api_tests.sh:19](repo/API_tests/checkins_api_tests.sh:19), [repo/API_tests/critical_actions_api_tests.sh:18](repo/API_tests/critical_actions_api_tests.sh:18).
- Playwright cross-role e2e does UI plus direct backend requests: [repo/frontend/e2e/cross-role-order-workflow.spec.ts:45](repo/frontend/e2e/cross-role-order-workflow.spec.ts:45).

### 2) HTTP with Mocking
- **None found** for backend API HTTP tests.

### 3) Non-HTTP (Unit/Integration without HTTP)
- Backend service/security/util unit tests with mocks (Mockito): e.g., [repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37), [repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37).
- Frontend component/guard specs with mocked providers/spies (Jasmine/TestBed): e.g., [repo/frontend/src/app/features/documents/document-center/document-center.component.spec.ts:34](repo/frontend/src/app/features/documents/document-center/document-center.component.spec.ts:34), [repo/frontend/src/app/core/guards/auth.guard.spec.ts:17](repo/frontend/src/app/core/guards/auth.guard.spec.ts:17).

## Mock Detection
- Backend Mockito mocks:
  - `OrderServiceTest` mocks repositories/services [repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37).
  - `CriticalActionServiceTest` mocks repositories/services [repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37).
  - `OrderStateMachineServiceTest` mock repository [repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderStateMachineServiceTest.java:17](repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderStateMachineServiceTest.java:17).
  - `DocumentNumberingServiceTest` mock repository [repo/backend/src/test/java/com/pharmaprocure/portal/service/DocumentNumberingServiceTest.java:19](repo/backend/src/test/java/com/pharmaprocure/portal/service/DocumentNumberingServiceTest.java:19).
- Frontend DI overrides and spies:
  - `useValue` provider overrides + `jasmine.createSpy` in component specs (example: [repo/frontend/src/app/features/admin/admin-page/admin-page.component.spec.ts:26](repo/frontend/src/app/features/admin/admin-page/admin-page.component.spec.ts:26), [repo/frontend/src/app/features/approvals/approvals-page/approvals-page.component.spec.ts:37](repo/frontend/src/app/features/approvals/approvals-page/approvals-page.component.spec.ts:37)).
- No `jest.mock`, `vi.mock`, `sinon.stub` found in inspected test files.

## Coverage Summary
- Total endpoints: **60**
- Endpoints with HTTP tests: **46**
- Endpoints with TRUE no-mock HTTP tests: **46**
- HTTP coverage: **76.67%** (`46/60`)
- True API coverage: **76.67%** (`46/60`)

Uncovered endpoints (14):
- `GET /api/catalog/products`
- `GET /api/meta/version`
- `GET /api/documents/review`
- `GET /api/documents/types`
- `GET /api/documents/templates`
- `GET /api/documents/approval-queue`
- `GET /api/documents/archive`
- `PUT /api/documents/{documentId}`
- `GET /api/admin/permissions`
- `GET /api/admin/document-types`
- `PUT /api/admin/document-types/{id}`
- `GET /api/admin/reason-codes`
- `POST /api/admin/reason-codes`
- `POST /api/orders/{orderId}/cancel`

## Unit Test Summary
### Backend Unit Tests
- Test files (non-HTTP unit):
  - Services: `AuthorizationBoundaryServiceTest`, `CaptchaServiceTest`, `CheckInRevisionDiffServiceTest`, `CriticalActionServiceTest`, `DocumentApprovalWorkflowServiceTest`, `DocumentHashingServiceTest`, `DocumentNumberingServiceTest`, `DocumentSignatureServiceTest`, `LoginAttemptPolicyServiceTest`, `OrderQuantityServiceTest`, `OrderServiceTest`, `OrderStateMachineServiceTest`, `PasswordPolicyServiceTest`
  - Security: `PermissionAuthorizationServiceTest`, `RateLimitFilterTest`
  - Util: `MaskingUtilsTest`
- Coverage by module type:
  - Controllers: mostly covered via HTTP integration tests, not direct unit tests.
  - Services: broad but uneven; key domain services covered for orders, critical actions, policy, doc workflows.
  - Repositories: not directly unit-tested (expected for Spring Data, but no repository-focused contract tests).
  - Auth/guards/middleware: backend security unit + integration present (`RateLimitFilterTest`, `PermissionAuthorizationServiceTest`, `RateLimitIntegrationTest`, `SecurityHeadersIntegrationTest`).
- Important backend modules not tested directly:
  - `AuthService`, `AdminService`, `CheckInService`, `DocumentCenterService`, `ProductCatalogService`, `MetaService` (no corresponding `*ServiceTest` files under backend test tree).

### Frontend Unit Tests (STRICT REQUIREMENT)
- Frontend test files found: **13 unit spec files** under `repo/frontend/src/**/*.spec.ts` including components and guards (examples: [repo/frontend/src/app/features/check-ins/check-ins-page/check-ins-page.component.spec.ts:1](repo/frontend/src/app/features/check-ins/check-ins-page/check-ins-page.component.spec.ts:1), [repo/frontend/src/app/core/guards/role.guard.spec.ts:1](repo/frontend/src/app/core/guards/role.guard.spec.ts:1)).
- Framework/tools detected:
  - Jasmine + Karma (`jasmine-core`, `karma*`) [repo/frontend/package.json:37](repo/frontend/package.json:37), [repo/frontend/angular.json:86](repo/frontend/angular.json:86), [repo/frontend/tsconfig.spec.json:7](repo/frontend/tsconfig.spec.json:7)
  - Angular TestBed usage [repo/frontend/src/app/app.component.spec.ts:1](repo/frontend/src/app/app.component.spec.ts:1)
- Components/modules covered:
  - Components: login, admin-page, approvals-page, check-ins-page, document-center, order-workspace, fulfillment-orders, returns, review-orders, app component.
  - Routing/guards: `app.routes`, `auth.guard`, `role.guard`.
- Important frontend modules/components not tested:
  - `dashboard.component.ts`, `finance-orders.component.ts`, `receipts.component.ts`, `order-detail.component.ts`, `shell.component.ts`, `unauthorized.component.ts`.
  - Core services/interceptors largely untested directly: `auth.service.ts`, `order.service.ts`, `document-center.service.ts`, `check-in.service.ts`, `admin.service.ts`, `critical-action.service.ts`, and interceptors in `core/interceptors/*.ts`.

**Frontend unit tests: PRESENT**

Strict sufficiency verdict for fullstack: **CRITICAL GAP** (frontend tests exist but are shallow/mocked and leave critical UI + service/interceptor surfaces untested).

### Cross-Layer Observation
- Backend API coverage is materially stronger than frontend unit depth.
- There are only 2 frontend e2e specs ([repo/frontend/e2e/cross-role-order-workflow.spec.ts:42](repo/frontend/e2e/cross-role-order-workflow.spec.ts:42), [repo/frontend/e2e/document-and-checkin-flow.spec.ts:24](repo/frontend/e2e/document-and-checkin-flow.spec.ts:24)); this does not balance missing frontend service/interceptor/unit depth.

## Tests Check
### API Observability Check
- Strong in many backend integration tests: explicit method/path + request payload + response assertions (e.g., [repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:41](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:41)).
- Weak areas:
  - Some tests assert only status (e.g., unauth checks in [repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:19](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:19)).
  - Shell API tests often use substring checks rather than structured field assertions (e.g., [repo/API_tests/order_lifecycle_api_tests.sh:126](repo/API_tests/order_lifecycle_api_tests.sh:126)).

### Test Quality & Sufficiency
- Success paths: present (order lifecycle, documents, check-ins, approvals).
- Failure paths: present (validation, auth, scope, rate-limit, lockout, duplicate actions).
- Edge cases: partially present (idempotency, concurrency, sanitization).
- Validation/auth/permissions: present across integration and API scripts.
- Integration boundaries: partially strong; e2e exists but limited breadth.
- Over-mocking risk: high in unit layer (backend Mockito and frontend service spies), but API HTTP layer is largely no-mock.

### `run_tests.sh` Check
- Docker-based orchestration: **OK** ([repo/run_tests.sh:26](repo/run_tests.sh:26)).
- Local dependency requirement: **FLAG** (`bash`, `curl`, and especially `python3` required by API scripts, e.g., [repo/API_tests/auth_api_tests.sh:24](repo/API_tests/auth_api_tests.sh:24)).

### End-to-End Expectations (Fullstack)
- Real FE↔BE tests exist but limited to two Playwright scenarios; partial compensation only.
- Missing breadth across critical frontend pages/services keeps fullstack e2e confidence moderate.

## Test Coverage Score (0–100)
- **68/100**

## Score Rationale
- Positive: high true HTTP API coverage (46/60), real no-mock integration/API testing, meaningful negative-path and security tests.
- Negative: 14 uncovered endpoints, critical uncovered admin/document surfaces, one uncovered state-changing order endpoint (`POST /api/orders/{orderId}/cancel`), frontend unit sufficiency gap for a fullstack system, and local non-container dependencies in test scripts.

## Key Gaps
- Uncovered endpoints concentrated in admin/document metadata/config and product/meta probes.
- No direct test for `POST /api/orders/{orderId}/cancel`.
- Frontend critical modules/services/interceptors not unit-tested.
- API script assertions are often string-based rather than schema/field-specific.

## Confidence & Assumptions
- Confidence: **High** for endpoint inventory and direct path-coverage mapping from static code.
- Assumptions:
  - MockMvc `@SpringBootTest` tests are treated as true no-mock HTTP-layer tests (in-process HTTP simulation with real route handlers).
  - Coverage is based strictly on visible method/path invocations in repository test files and scripts; no runtime execution performed.

---

# README Audit

## README Location
- Found at required path: [repo/README.md](repo/README.md)

## Hard Gate Evaluation
### Formatting
- Pass: markdown is readable and structured with sections/headings/list blocks.

### Startup Instructions (Backend/Fullstack)
- **FAIL (Hard Gate)**
- README uses `docker compose up` ([repo/README.md:16](repo/README.md:16)), but strict gate requires explicit `docker-compose up` string.

### Access Method
- Pass: includes frontend and backend URL+port ([repo/README.md:21](repo/README.md:21), [repo/README.md:22](repo/README.md:22)).

### Verification Method
- Pass: includes `curl` checks and API/UI flows ([repo/README.md:37](repo/README.md:37), [repo/README.md:45](repo/README.md:45)).

### Environment Rules (No install/manual DB setup)
- Pass: no npm/pip/apt/manual DB setup instructions in README.

### Demo Credentials (Auth conditional)
- Auth exists (login endpoints and seeded users): [repo/backend/src/main/java/com/pharmaprocure/portal/controller/AuthController.java:49](repo/backend/src/main/java/com/pharmaprocure/portal/controller/AuthController.java:49), [repo/backend/src/main/resources/db/migration/V1__init_schema_and_seed.sql:41](repo/backend/src/main/resources/db/migration/V1__init_schema_and_seed.sql:41).
- Credentials + password + roles listed in README: [repo/README.md:67](repo/README.md:67) through [repo/README.md:74](repo/README.md:74).
- Pass.

## Engineering Quality
- Tech stack clarity: good ([repo/README.md:3](repo/README.md:3), [repo/README.md:117](repo/README.md:117)).
- Architecture explanation: good high-level.
- Testing instructions: present but not strict about container-only toolchain (API scripts require host `python3`/`curl`, inferred from scripts).
- Security/roles/workflows: clearly documented.
- Presentation quality: solid and organized.

## High Priority Issues
- README does not explicitly declare project type at top using required token (`backend|fullstack|web|android|ios|desktop`).
- Required startup literal `docker-compose up` is missing; only `docker compose up` appears.

## Medium Priority Issues
- Verification section depends on local script toolchain details not surfaced (host `python3` dependency in API scripts).
- README API surface lists endpoints that currently lack direct test coverage (admin/document metadata/config endpoints).

## Low Priority Issues
- Minor ambiguity between `docker compose up` and `docker compose up --build` usage paths.

## Hard Gate Failures
1. Missing strict startup command literal `docker-compose up` for fullstack/backend.

## README Verdict
- **FAIL**

## Final Verdicts
- Test Coverage Audit Verdict: **PARTIAL PASS (with CRITICAL GAPS)**
- README Audit Verdict: **FAIL**
