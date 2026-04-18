# Test Coverage Audit

## Project Type Detection
- README declares required token at top: **fullstack** ([repo/README.md:3](repo/README.md:3)).
- Inferred architecture is also fullstack from Angular + Spring controllers + Docker Compose ([repo/frontend/angular.json:7](repo/frontend/angular.json:7), [repo/backend/src/main/java/com/pharmaprocure/portal/controller/OrderController.java:31](repo/backend/src/main/java/com/pharmaprocure/portal/controller/OrderController.java:31), [repo/README.md:15](repo/README.md:15)).

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
| GET /api/catalog/products | yes | true no-mock HTTP | coverage_api_tests.sh | `curl ... /api/catalog/products` ([repo/API_tests/coverage_api_tests.sh:165](repo/API_tests/coverage_api_tests.sh:165)) |
| GET /api/health | yes | true no-mock HTTP | SecurityHeadersIntegrationTest, coverage_api_tests.sh | `get("/api/health")` ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/SecurityHeadersIntegrationTest.java:14](repo/backend/src/test/java/com/pharmaprocure/portal/integration/SecurityHeadersIntegrationTest.java:14)); `curl ... /api/health` ([repo/API_tests/coverage_api_tests.sh:159](repo/API_tests/coverage_api_tests.sh:159)) |
| GET /api/meta/version | yes | true no-mock HTTP | coverage_api_tests.sh | `curl ... /api/meta/version` ([repo/API_tests/coverage_api_tests.sh:152](repo/API_tests/coverage_api_tests.sh:152)) |
| GET /api/orders/workspace | yes | true no-mock HTTP | auth_api_tests.sh | `request GET "/api/orders/workspace"` ([repo/API_tests/auth_api_tests.sh:95](repo/API_tests/auth_api_tests.sh:95)) |
| GET /api/documents/review | yes | true no-mock HTTP | coverage_api_tests.sh | `curl ... /api/documents/review` ([repo/API_tests/coverage_api_tests.sh:217](repo/API_tests/coverage_api_tests.sh:217)) |
| GET /api/admin/panel | yes | true no-mock HTTP | auth_api_tests.sh | `request GET "/api/admin/panel"` ([repo/API_tests/auth_api_tests.sh:98](repo/API_tests/auth_api_tests.sh:98)) |
| GET /api/auth/csrf | yes | true no-mock HTTP | auth_api_tests.sh, coverage_api_tests.sh, Playwright e2e | ([repo/API_tests/auth_api_tests.sh:23](repo/API_tests/auth_api_tests.sh:23)), ([repo/API_tests/coverage_api_tests.sh:408](repo/API_tests/coverage_api_tests.sh:408)), ([repo/frontend/e2e/cross-role-order-workflow.spec.ts:12](repo/frontend/e2e/cross-role-order-workflow.spec.ts:12)) |
| GET /api/auth/captcha | yes | true no-mock HTTP | auth_api_tests.sh, coverage_api_tests.sh | ([repo/API_tests/auth_api_tests.sh:123](repo/API_tests/auth_api_tests.sh:123)), ([repo/API_tests/coverage_api_tests.sh:416](repo/API_tests/coverage_api_tests.sh:416)) |
| POST /api/auth/login | yes | true no-mock HTTP | AuthControllerIntegrationTest, SanitizationIntegrationTest, RateLimitIntegrationTest, auth_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/AuthControllerIntegrationTest.java:20](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AuthControllerIntegrationTest.java:20)), ([repo/API_tests/auth_api_tests.sh:88](repo/API_tests/auth_api_tests.sh:88)) |
| POST /api/auth/logout | yes | true no-mock HTTP | auth_api_tests.sh, Playwright e2e | ([repo/API_tests/auth_api_tests.sh:162](repo/API_tests/auth_api_tests.sh:162)), ([repo/frontend/e2e/document-and-checkin-flow.spec.ts:15](repo/frontend/e2e/document-and-checkin-flow.spec.ts:15)) |
| GET /api/auth/me | yes | true no-mock HTTP | auth_api_tests.sh, coverage_api_tests.sh | ([repo/API_tests/auth_api_tests.sh:92](repo/API_tests/auth_api_tests.sh:92)), ([repo/API_tests/coverage_api_tests.sh:412](repo/API_tests/coverage_api_tests.sh:412)) |
| GET /api/check-ins | yes | true no-mock HTTP | NegativePathIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:31](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:31)), ([repo/API_tests/coverage_api_tests.sh:401](repo/API_tests/coverage_api_tests.sh:401)) |
| POST /api/check-ins | yes | true no-mock HTTP | CheckInControllerIntegrationTest, checkins_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:117](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:117)), ([repo/API_tests/checkins_api_tests.sh:60](repo/API_tests/checkins_api_tests.sh:60)) |
| PUT /api/check-ins/{checkInId} | yes | true no-mock HTTP | CheckInControllerIntegrationTest, checkins_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:60](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:60)), ([repo/API_tests/checkins_api_tests.sh:77](repo/API_tests/checkins_api_tests.sh:77)) |
| GET /api/check-ins/{checkInId} | yes | true no-mock HTTP | CheckInControllerIntegrationTest, checkins_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:84](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CheckInControllerIntegrationTest.java:84)), ([repo/API_tests/checkins_api_tests.sh:81](repo/API_tests/checkins_api_tests.sh:81)) |
| GET /api/check-ins/{checkInId}/attachments/{attachmentId}/download | yes | true no-mock HTTP | checkins_api_tests.sh | ([repo/API_tests/checkins_api_tests.sh:90](repo/API_tests/checkins_api_tests.sh:90)) |
| GET /api/documents/types | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:182](repo/API_tests/coverage_api_tests.sh:182)) |
| GET /api/documents/templates | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:193](repo/API_tests/coverage_api_tests.sh:193)) |
| POST /api/documents/templates | yes | true no-mock HTTP | document_center_api_tests.sh, coverage_api_tests.sh | ([repo/API_tests/document_center_api_tests.sh:76](repo/API_tests/document_center_api_tests.sh:76)), ([repo/API_tests/coverage_api_tests.sh:235](repo/API_tests/coverage_api_tests.sh:235)) |
| GET /api/documents | yes | true no-mock HTTP | NegativePathIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:25](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:25)), ([repo/API_tests/coverage_api_tests.sh:210](repo/API_tests/coverage_api_tests.sh:210)) |
| GET /api/documents/approval-queue | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:226](repo/API_tests/coverage_api_tests.sh:226)) |
| GET /api/documents/archive | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:196](repo/API_tests/coverage_api_tests.sh:196)) |
| POST /api/documents | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest, document_center_api_tests.sh, critical_actions_api_tests.sh, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:87](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:87)), ([repo/API_tests/document_center_api_tests.sh:79](repo/API_tests/document_center_api_tests.sh:79)), ([repo/API_tests/coverage_api_tests.sh:241](repo/API_tests/coverage_api_tests.sh:241)) |
| PUT /api/documents/{documentId} | yes | true no-mock HTTP | coverage_api_tests.sh | `multipart_request PUT "/api/documents/$DOC_ID"` ([repo/API_tests/coverage_api_tests.sh:247](repo/API_tests/coverage_api_tests.sh:247)) |
| GET /api/documents/{documentId} | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest, NegativePathIntegrationTest, document_center_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:65](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:65)), ([repo/API_tests/document_center_api_tests.sh:114](repo/API_tests/document_center_api_tests.sh:114)) |
| POST /api/documents/{documentId}/submit-approval | yes | true no-mock HTTP | document_center_api_tests.sh, critical_actions_api_tests.sh | ([repo/API_tests/document_center_api_tests.sh:93](repo/API_tests/document_center_api_tests.sh:93)) |
| POST /api/documents/{documentId}/approve | yes | true no-mock HTTP | document_center_api_tests.sh, critical_actions_api_tests.sh | ([repo/API_tests/document_center_api_tests.sh:100](repo/API_tests/document_center_api_tests.sh:100)) |
| POST /api/documents/{documentId}/archive | yes | true no-mock HTTP | document_center_api_tests.sh, critical_actions_api_tests.sh | ([repo/API_tests/document_center_api_tests.sh:103](repo/API_tests/document_center_api_tests.sh:103)) |
| GET /api/documents/{documentId}/preview | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest, document_center_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:42](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:42)), ([repo/API_tests/document_center_api_tests.sh:107](repo/API_tests/document_center_api_tests.sh:107)) |
| GET /api/documents/{documentId}/content | yes | true no-mock HTTP | DocumentCenterControllerIntegrationTest | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:46](repo/backend/src/test/java/com/pharmaprocure/portal/integration/DocumentCenterControllerIntegrationTest.java:46)) |
| GET /api/documents/{documentId}/download | yes | true no-mock HTTP | document_center_api_tests.sh | ([repo/API_tests/document_center_api_tests.sh:111](repo/API_tests/document_center_api_tests.sh:111)) |
| GET /api/admin/users | yes | true no-mock HTTP | NegativePathIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:43](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:43)), ([repo/API_tests/coverage_api_tests.sh:329](repo/API_tests/coverage_api_tests.sh:329)) |
| PUT /api/admin/users/{id} | yes | true no-mock HTTP | AdminControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:44](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:44)), ([repo/API_tests/coverage_api_tests.sh:340](repo/API_tests/coverage_api_tests.sh:340)) |
| PUT /api/admin/users/{id}/password | yes | true no-mock HTTP | AdminControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:59](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:59)), ([repo/API_tests/coverage_api_tests.sh:342](repo/API_tests/coverage_api_tests.sh:342)) |
| GET /api/admin/permissions | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:259](repo/API_tests/coverage_api_tests.sh:259)) |
| GET /api/admin/state-machine | yes | true no-mock HTTP | AdminControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:34](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:34)), ([repo/API_tests/coverage_api_tests.sh:317](repo/API_tests/coverage_api_tests.sh:317)) |
| PUT /api/admin/state-machine/{id} | yes | true no-mock HTTP | AdminControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:25](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AdminControllerIntegrationTest.java:25)), ([repo/API_tests/coverage_api_tests.sh:325](repo/API_tests/coverage_api_tests.sh:325)) |
| GET /api/admin/document-types | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:272](repo/API_tests/coverage_api_tests.sh:272)) |
| PUT /api/admin/document-types/{id} | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:287](repo/API_tests/coverage_api_tests.sh:287)) |
| GET /api/admin/reason-codes | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:298](repo/API_tests/coverage_api_tests.sh:298)) |
| POST /api/admin/reason-codes | yes | true no-mock HTTP | coverage_api_tests.sh | ([repo/API_tests/coverage_api_tests.sh:303](repo/API_tests/coverage_api_tests.sh:303)) |
| PUT /api/admin/reason-codes/{id} | yes | true no-mock HTTP | OrderControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:156](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:156)), ([repo/API_tests/coverage_api_tests.sh:308](repo/API_tests/coverage_api_tests.sh:308)) |
| GET /api/critical-actions | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, NegativePathIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:43](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:43)), ([repo/API_tests/coverage_api_tests.sh:405](repo/API_tests/coverage_api_tests.sh:405)) |
| GET /api/critical-actions/{requestId} | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, NegativePathIntegrationTest, critical_actions_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:265](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:265)), ([repo/API_tests/critical_actions_api_tests.sh:100](repo/API_tests/critical_actions_api_tests.sh:100)) |
| POST /api/critical-actions | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, critical_actions_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:143](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:143)), ([repo/API_tests/critical_actions_api_tests.sh:41](repo/API_tests/critical_actions_api_tests.sh:41)) |
| POST /api/critical-actions/{requestId}/decision | yes | true no-mock HTTP | CriticalActionControllerIntegrationTest, IdempotencyIntegrationTest, ConcurrencyIntegrationTest, critical_actions_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:70](repo/backend/src/test/java/com/pharmaprocure/portal/integration/CriticalActionControllerIntegrationTest.java:70)), ([repo/API_tests/critical_actions_api_tests.sh:47](repo/API_tests/critical_actions_api_tests.sh:47)) |
| GET /api/orders | yes | true no-mock HTTP | PaginationIntegrationTest, NegativePathIntegrationTest, SecurityHeadersIntegrationTest, RateLimitIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/PaginationIntegrationTest.java:20](repo/backend/src/test/java/com/pharmaprocure/portal/integration/PaginationIntegrationTest.java:20)), ([repo/API_tests/coverage_api_tests.sh:352](repo/API_tests/coverage_api_tests.sh:352)) |
| GET /api/orders/{orderId} | yes | true no-mock HTTP | OrderControllerIntegrationTest, NegativePathIntegrationTest, CriticalActionControllerIntegrationTest, API scripts | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:29](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:29)), ([repo/API_tests/order_lifecycle_api_tests.sh:140](repo/API_tests/order_lifecycle_api_tests.sh:140)) |
| GET /api/orders/reason-codes | yes | true no-mock HTTP | OrderControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:194](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:194)), ([repo/API_tests/coverage_api_tests.sh:356](repo/API_tests/coverage_api_tests.sh:356)) |
| POST /api/orders | yes | true no-mock HTTP | SanitizationIntegrationTest, order_lifecycle_api_tests.sh, critical_actions_api_tests.sh, Playwright e2e, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/SanitizationIntegrationTest.java:64](repo/backend/src/test/java/com/pharmaprocure/portal/integration/SanitizationIntegrationTest.java:64)), ([repo/API_tests/order_lifecycle_api_tests.sh:71](repo/API_tests/order_lifecycle_api_tests.sh:71)), ([repo/API_tests/coverage_api_tests.sh:360](repo/API_tests/coverage_api_tests.sh:360)) |
| POST /api/orders/{orderId}/submit-review | yes | true no-mock HTTP | IdempotencyIntegrationTest, ConcurrencyIntegrationTest, order_lifecycle_api_tests.sh, Playwright e2e | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:31](repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:31)), ([repo/API_tests/order_lifecycle_api_tests.sh:79](repo/API_tests/order_lifecycle_api_tests.sh:79)) |
| POST /api/orders/{orderId}/cancel | yes | true no-mock HTTP | coverage_api_tests.sh | `json_request POST "/api/orders/$CANCEL_ORDER_ID/cancel"` ([repo/API_tests/coverage_api_tests.sh:364](repo/API_tests/coverage_api_tests.sh:364)) |
| POST /api/orders/{orderId}/approve | yes | true no-mock HTTP | OrderControllerIntegrationTest, IdempotencyIntegrationTest, order_lifecycle_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:205](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:205)), ([repo/API_tests/order_lifecycle_api_tests.sh:82](repo/API_tests/order_lifecycle_api_tests.sh:82)) |
| POST /api/orders/{orderId}/record-payment | yes | true no-mock HTTP | IdempotencyIntegrationTest, order_lifecycle_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:117](repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:117)), ([repo/API_tests/order_lifecycle_api_tests.sh:88](repo/API_tests/order_lifecycle_api_tests.sh:88)) |
| POST /api/orders/{orderId}/pick-pack | yes | true no-mock HTTP | IdempotencyIntegrationTest, order_lifecycle_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:140](repo/backend/src/test/java/com/pharmaprocure/portal/integration/IdempotencyIntegrationTest.java:140)), ([repo/API_tests/order_lifecycle_api_tests.sh:94](repo/API_tests/order_lifecycle_api_tests.sh:94)) |
| POST /api/orders/{orderId}/shipments | yes | true no-mock HTTP | OrderControllerIntegrationTest, order_lifecycle_api_tests.sh, Playwright e2e | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:41](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:41)), ([repo/API_tests/order_lifecycle_api_tests.sh:108](repo/API_tests/order_lifecycle_api_tests.sh:108)) |
| POST /api/orders/{orderId}/receipts | yes | true no-mock HTTP | OrderControllerIntegrationTest, order_lifecycle_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:54](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:54)), ([repo/API_tests/order_lifecycle_api_tests.sh:111](repo/API_tests/order_lifecycle_api_tests.sh:111)) |
| POST /api/orders/{orderId}/returns | yes | true no-mock HTTP | OrderControllerIntegrationTest, order_lifecycle_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:170](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:170)), ([repo/API_tests/order_lifecycle_api_tests.sh:120](repo/API_tests/order_lifecycle_api_tests.sh:120)) |
| POST /api/orders/{orderId}/after-sales-cases | yes | true no-mock HTTP | OrderControllerIntegrationTest, coverage_api_tests.sh | ([repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:182](repo/backend/src/test/java/com/pharmaprocure/portal/integration/OrderControllerIntegrationTest.java:182)), ([repo/API_tests/coverage_api_tests.sh:391](repo/API_tests/coverage_api_tests.sh:391)) |
| GET /api/orders/{orderId}/traceability | yes | true no-mock HTTP | order_lifecycle_api_tests.sh | ([repo/API_tests/order_lifecycle_api_tests.sh:123](repo/API_tests/order_lifecycle_api_tests.sh:123)) |

## API Test Classification
### 1) True No-Mock HTTP
- Backend integration tests boot full Spring context via `@SpringBootTest` + `@AutoConfigureMockMvc` (no `@MockBean` in integration package): [repo/backend/src/test/java/com/pharmaprocure/portal/integration/AbstractMockMvcIntegrationTest.java:35](repo/backend/src/test/java/com/pharmaprocure/portal/integration/AbstractMockMvcIntegrationTest.java:35).
- Shell suites use `curl` against live HTTP routes: [repo/API_tests/auth_api_tests.sh:85](repo/API_tests/auth_api_tests.sh:85), [repo/API_tests/order_lifecycle_api_tests.sh:71](repo/API_tests/order_lifecycle_api_tests.sh:71), [repo/API_tests/document_center_api_tests.sh:79](repo/API_tests/document_center_api_tests.sh:79), [repo/API_tests/checkins_api_tests.sh:60](repo/API_tests/checkins_api_tests.sh:60), [repo/API_tests/critical_actions_api_tests.sh:41](repo/API_tests/critical_actions_api_tests.sh:41), [repo/API_tests/coverage_api_tests.sh:152](repo/API_tests/coverage_api_tests.sh:152).
- Playwright e2e executes browser flow plus API requests: [repo/frontend/e2e/cross-role-order-workflow.spec.ts:45](repo/frontend/e2e/cross-role-order-workflow.spec.ts:45).

### 2) HTTP with Mocking
- None found for backend API route tests.

### 3) Non-HTTP (Unit/Integration without HTTP)
- Backend service/security/unit tests with Mockito mocks: [repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37), [repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37).
- Frontend unit specs with TestBed and DI stubs/spies: [repo/frontend/src/app/features/admin/admin-page/admin-page.component.spec.ts:26](repo/frontend/src/app/features/admin/admin-page/admin-page.component.spec.ts:26), [repo/frontend/src/app/features/orders/order-detail/order-detail.component.spec.ts:33](repo/frontend/src/app/features/orders/order-detail/order-detail.component.spec.ts:33).

## Mock Detection
- Backend Mockito mocks:
  - `OrderServiceTest` repository/service collaborators mocked: [repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderServiceTest.java:37).
  - `CriticalActionServiceTest` repositories + services mocked: [repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37](repo/backend/src/test/java/com/pharmaprocure/portal/service/CriticalActionServiceTest.java:37).
  - `OrderStateMachineServiceTest` mocked repository: [repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderStateMachineServiceTest.java:17](repo/backend/src/test/java/com/pharmaprocure/portal/service/OrderStateMachineServiceTest.java:17).
  - `DocumentNumberingServiceTest` mocked repository: [repo/backend/src/test/java/com/pharmaprocure/portal/service/DocumentNumberingServiceTest.java:19](repo/backend/src/test/java/com/pharmaprocure/portal/service/DocumentNumberingServiceTest.java:19).
- Frontend DI overrides and spies (`useValue`, `jasmine.createSpy`) are pervasive in component/service specs: [repo/frontend/src/app/features/documents/document-center/document-center.component.spec.ts:34](repo/frontend/src/app/features/documents/document-center/document-center.component.spec.ts:34), [repo/frontend/src/app/layout/shell/shell.component.spec.ts:23](repo/frontend/src/app/layout/shell/shell.component.spec.ts:23), [repo/frontend/src/app/core/services/order.service.spec.ts:12](repo/frontend/src/app/core/services/order.service.spec.ts:12).
- No `jest.mock`, `vi.mock`, or `sinon.stub` found in inspected test files.

## Coverage Summary
- Total endpoints: **60**
- Endpoints with HTTP tests: **60**
- Endpoints with TRUE no-mock HTTP tests: **60**
- HTTP coverage: **100.00%** (`60/60`)
- True API coverage: **100.00%** (`60/60`)

Uncovered endpoints: **None** (based on static route-to-test evidence).

## Unit Test Summary
### Backend Unit Tests
- Non-HTTP backend unit test files include:
  - Services: `AuthorizationBoundaryServiceTest`, `CaptchaServiceTest`, `CheckInRevisionDiffServiceTest`, `CriticalActionServiceTest`, `DocumentApprovalWorkflowServiceTest`, `DocumentHashingServiceTest`, `DocumentNumberingServiceTest`, `DocumentSignatureServiceTest`, `LoginAttemptPolicyServiceTest`, `OrderQuantityServiceTest`, `OrderServiceTest`, `OrderStateMachineServiceTest`, `PasswordPolicyServiceTest`
  - Security: `PermissionAuthorizationServiceTest`, `RateLimitFilterTest`
  - Util: `MaskingUtilsTest`
- Module coverage:
  - Controllers: primarily integration-tested (HTTP) rather than direct unit tests.
  - Services: broad for order/critical-action/policy/document utility services.
  - Repositories: no dedicated repository contract tests.
  - Auth/guards/middleware: backend security + integration coverage exists (`RateLimitFilterTest`, `PermissionAuthorizationServiceTest`, `RateLimitIntegrationTest`, `SecurityHeadersIntegrationTest`).
- Important backend modules not directly unit-tested:
  - `AuthService`, `AdminService`, `CheckInService`, `DocumentCenterService`, `ProductCatalogService`, `MetaService`.

### Frontend Unit Tests (STRICT REQUIREMENT)
- Frontend unit tests are present with direct file-level evidence (`*.spec.ts`) under `repo/frontend/src`.
- Framework/tools detected:
  - Jasmine + Karma: [repo/frontend/package.json:37](repo/frontend/package.json:37), [repo/frontend/angular.json:86](repo/frontend/angular.json:86).
  - Angular TestBed usage: [repo/frontend/src/app/app.component.spec.ts:1](repo/frontend/src/app/app.component.spec.ts:1).
- Components/modules covered include prior gaps:
  - `dashboard`, `finance-orders`, `receipts`, `order-detail`, `shell`, `unauthorized` ([repo/frontend/src/app/features/dashboard/dashboard.component.spec.ts:1](repo/frontend/src/app/features/dashboard/dashboard.component.spec.ts:1), [repo/frontend/src/app/features/orders/finance/finance-orders.component.spec.ts:1](repo/frontend/src/app/features/orders/finance/finance-orders.component.spec.ts:1), [repo/frontend/src/app/features/orders/receipts/receipts.component.spec.ts:1](repo/frontend/src/app/features/orders/receipts/receipts.component.spec.ts:1), [repo/frontend/src/app/features/orders/order-detail/order-detail.component.spec.ts:1](repo/frontend/src/app/features/orders/order-detail/order-detail.component.spec.ts:1), [repo/frontend/src/app/layout/shell/shell.component.spec.ts:1](repo/frontend/src/app/layout/shell/shell.component.spec.ts:1), [repo/frontend/src/app/features/auth/unauthorized/unauthorized.component.spec.ts:1](repo/frontend/src/app/features/auth/unauthorized/unauthorized.component.spec.ts:1)).
  - Core services/interceptors include auth/order/document/check-in/admin/critical-action and interceptor specs ([repo/frontend/src/app/core/services/auth.service.spec.ts:1](repo/frontend/src/app/core/services/auth.service.spec.ts:1), [repo/frontend/src/app/core/interceptors/error.interceptor.spec.ts:1](repo/frontend/src/app/core/interceptors/error.interceptor.spec.ts:1)).
- Important frontend modules still without direct unit specs:
  - `api.service.ts`, `csrf.service.ts`, `icon.service.ts`, `loading.service.ts` (service files exist without matching `*.spec.ts` in same directory: [repo/frontend/src/app/core/services](repo/frontend/src/app/core/services)).

**Frontend unit tests: PRESENT**

Strict sufficiency verdict for fullstack: **PASS WITH REMAINING DEPTH RISKS** (breadth improved materially; substantial use of spies/DI overrides still limits realism in frontend unit layer).

### Cross-Layer Observation
- Backend API route coverage is now complete (60/60).
- Frontend unit breadth is materially improved vs prior baseline and now includes previously missing critical screens/services/interceptors.
- E2E breadth is still modest (2 specs), but compensated by strong route-level integration/API coverage.

## Tests Check
### API Observability Check
- Strong in coverage and lifecycle scripts: explicit method/path, payloads, status checks, and selected JSON-field assertions ([repo/API_tests/coverage_api_tests.sh:247](repo/API_tests/coverage_api_tests.sh:247), [repo/API_tests/order_lifecycle_api_tests.sh:111](repo/API_tests/order_lifecycle_api_tests.sh:111)).
- Weak areas remain:
  - Some tests still assert only status (especially access-control negative checks), e.g., [repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:19](repo/backend/src/test/java/com/pharmaprocure/portal/integration/NegativePathIntegrationTest.java:19).
  - A portion of shell assertions remain substring-based (`contains`) rather than field/schema assertions, e.g., [repo/API_tests/order_lifecycle_api_tests.sh:126](repo/API_tests/order_lifecycle_api_tests.sh:126), [repo/API_tests/document_center_api_tests.sh:95](repo/API_tests/document_center_api_tests.sh:95).

### Test Quality & Sufficiency
- Success paths: strong and broad across orders/documents/check-ins/critical actions/auth/admin.
- Failure paths: present (invalid creds, CAPTCHA, lockout, forbidden scope, weak password, invalid transitions, invalid files).
- Edge cases: present for idempotency/concurrency/expiry/sanitization.
- Validation/auth/permissions: strong coverage.
- Over-mocking risk: still high in frontend unit layer and backend service-unit layer (Mockito/spies), but API HTTP layer itself remains effectively no-mock.

### `run_tests.sh` Check
- Docker-based orchestration: **OK** ([repo/run_tests.sh:13](repo/run_tests.sh:13), [repo/run_tests.sh:107](repo/run_tests.sh:107), [repo/run_tests.sh:153](repo/run_tests.sh:153)).
- Failure handling: **OK** (`set -Eeuo pipefail`, traps, step accounting, summary/fail-fast) ([repo/run_tests.sh:10](repo/run_tests.sh:10), [repo/run_tests.sh:52](repo/run_tests.sh:52), [repo/run_tests.sh:164](repo/run_tests.sh:164), [repo/run_tests.sh:189](repo/run_tests.sh:189)).
- Host dependency leakage in orchestrator: **No direct runtime dependency on host python/curl/java/node in `run_tests.sh`**; test scripts run inside Docker test-tools container ([repo/run_tests.sh:104](repo/run_tests.sh:104), [repo/run_tests.sh:153](repo/run_tests.sh:153)).

## Test Coverage Score (0–100)
- **92/100**

## Score Rationale
- Positive: full endpoint coverage (`60/60`), true route-level no-mock HTTP tests, robust negative-path and security checks, containerized orchestrator with explicit failure management.
- Deductions: frontend and service-level unit tests still rely heavily on spies/mocks, some assertions remain shallow/string-based, and e2e breadth remains limited to two scenarios.

## Key Gaps
- Increase structured JSON/schema assertions in shell scripts to reduce false positives from substring matching.
- Add direct unit tests for `api.service.ts`, `csrf.service.ts`, `icon.service.ts`, and `loading.service.ts`.
- Expand frontend e2e breadth beyond two workflows.

## Confidence & Assumptions
- Confidence: **High** for endpoint inventory and route-to-test mapping (static evidence directly present).
- Assumptions:
  - MockMvc `@SpringBootTest` route tests are classified as true HTTP-layer route tests (in-process transport simulation with real handlers).
  - Coverage is derived from static route invocation evidence in repository test files/scripts; no runtime execution performed.

---

# README Audit

## README Location
- Found at required path: [repo/README.md](repo/README.md)

## Hard Gate Evaluation
### Formatting
- Pass: readable markdown with clear sections and command blocks.

### Startup Instructions (Backend/Fullstack)
- Pass: includes required literal `docker-compose up` ([repo/README.md:20](repo/README.md:20)).

### Access Method
- Pass: frontend/backend URL + ports are documented ([repo/README.md:31](repo/README.md:31), [repo/README.md:32](repo/README.md:32)).

### Verification Method
- Pass: verification is explicit and Docker-contained, with a single primary command `bash run_tests.sh` and containerized ad-hoc/API checks ([repo/README.md:37](repo/README.md:37), [repo/README.md:44](repo/README.md:44), [repo/README.md:62](repo/README.md:62), [repo/README.md:74](repo/README.md:74)).

### Environment Rules (No install/manual DB setup, Docker-contained)
- Pass: README explicitly states host prerequisites are only bash + Docker/Compose and routes API/smoke/unit execution through containers ([repo/README.md:39](repo/README.md:39), [repo/README.md:49](repo/README.md:49), [repo/README.md:81](repo/README.md:81)).

### Demo Credentials (Auth conditional)
- Pass: auth exists and README includes username/password/roles ([repo/backend/src/main/java/com/pharmaprocure/portal/controller/AuthController.java:49](repo/backend/src/main/java/com/pharmaprocure/portal/controller/AuthController.java:49), [repo/backend/src/main/resources/db/migration/V1__init_schema_and_seed.sql:41](repo/backend/src/main/resources/db/migration/V1__init_schema_and_seed.sql:41), [repo/README.md:90](repo/README.md:90) through [repo/README.md:97](repo/README.md:97)).

## Engineering Quality
- Tech stack clarity: strong ([repo/README.md:5](repo/README.md:5), [repo/README.md:140](repo/README.md:140)).
- Architecture and workflows: clear and actionable.
- Testing instructions: clear and consistent with Docker-only execution model.
- Security/roles: well documented and explicit.
- Presentation quality: high.

## High Priority Issues
- None.

## Medium Priority Issues
- Minor ambiguity between `docker-compose up` and `docker compose up --build` usage paths remains, though both are valid.

## Low Priority Issues
- None material.

## Hard Gate Failures
- None.

## README Verdict
- **PASS**

## Final Verdicts
- Test Coverage Audit Verdict: **PASS (STRICT, WITH IMPROVEMENT OPPORTUNITIES)**
- README Audit Verdict: **PASS**
