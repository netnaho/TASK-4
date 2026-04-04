# PharmaProcure Server API Specification

This document details the complete REST API surface exposed by the PharmaProcure Compliance Procurement Portal backend services.

## Base URL
All API requests are prefixed by:
`http://localhost:8080` (or the corresponding production gateway).

---

## 1. Authentication & Security `/api/auth`
Responsible for session lifecycle, login lockouts, and CSRF token generation.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/auth/csrf` | Fetches session-bound CSRF token data (header and parameter name) | - |
| `GET` | `/api/auth/captcha?username={username}` | Fetches a CAPTCHA challenge requirement status for given username | - |
| `POST` | `/api/auth/login` | Authenticates user credentials and establishes HttpOnly cookie session | `LoginRequest` |
| `POST` | `/api/auth/logout` | Invalidates current user session and clears cookies | - |
| `GET` | `/api/auth/me` | Retrieves the currently active user profile and permission scope | - |

---

## 2. Orders & Procurement Lifecycle `/api/orders`
Manages the end-to-end procurement process, from drafting to final fulfillment and returns.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/orders` | Retrieves order summaries scoped to user's RBAC boundaries | - |
| `POST` | `/api/orders` | Creates a new draft procurement order | `CreateOrderRequest` |
| `GET` | `/api/orders/{id}` | Retrieves full order details, including line items and history | - |
| `GET` | `/api/orders/reason-codes?codeType={RETURN|AFTER_SALES}` | Lists active admin-managed reason codes for return and after-sales flows | - |
| `POST` | `/api/orders/{id}/submit-review`| Submits the drafted order for Quality/Finance review | - |
| `POST` | `/api/orders/{id}/cancel` | Requests cancellation or directly cancels an order | - |
| `POST` | `/api/orders/{id}/approve` | Approves or rejects a submitted order | `ReviewOrderRequest` |
| `POST` | `/api/orders/{id}/record-payment`| Records payment settlement via finance | `RecordPaymentRequest`|
| `POST` | `/api/orders/{id}/pick-pack` | Triggers the picking and packing fulfillment stage | - |
| `POST` | `/api/orders/{id}/shipments` | Creates a shipment manifest against packed goods | `ShipmentCreateRequest`|
| `POST` | `/api/orders/{id}/receipts` | Registers the receipt of shipped goods, handling variants | `ReceiptCreateRequest`|
| `POST` | `/api/orders/{id}/returns` | Initiates a return request using an active managed return reason code | `ReturnCreateRequest`|
| `POST` | `/api/orders/{id}/after-sales-cases`| Creates after-sales complaints (eg. temperature excursions) using an active managed reason code | `AfterSalesCaseCreateRequest`|
| `GET` | `/api/orders/{id}/traceability` | Generates traceability graph data for compliance | - |

---

## 3. Product Catalog `/api/catalog`
Provides available procurement items.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/catalog/products` | Retrieves available products for ordering | - |

---

## 4. Document Center `/api/documents`
Manages compliance-controlled documents, versioning, routing, and secured downloads.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/documents/types` | Lists configured document type taxonomies | - |
| `GET` | `/api/documents/templates` | Lists pre-approved document templates | - |
| `POST` | `/api/documents/templates` | Uploads/creates a new global document template | `CreateDocumentTemplateRequest`|
| `GET` | `/api/documents` | Lists document summaries accessible to user | - |
| `GET` | `/api/documents/approval-queue` | Lists documents awaiting the current user's approval | - |
| `GET` | `/api/documents/archive` | Lists formally archived and finalized documents | - |
| `POST` | `/api/documents` | Creates a new document draft | `MultipartFile file`, `String payload` |
| `PUT` | `/api/documents/{id}` | Updates existing document draft | `MultipartFile file`, `String payload` |
| `GET` | `/api/documents/{id}` | Retrieves document metadata details | - |
| `POST` | `/api/documents/{id}/submit-approval`| Routes draft into formal approval chain | - |
| `POST` | `/api/documents/{id}/approve` | Grants approval/rejection with inline signature | `ApproveDocumentRequest` |
| `POST` | `/api/documents/{id}/archive` | Formally moves approved document to archive state | - |
| `GET` | `/api/documents/{id}/preview` | Renders a watermarked preview of the document | - |
| `GET` | `/api/documents/{id}/content` | Retrieves inline preview bytes; supported preview types are returned with server-applied watermarking | - |
| `GET` | `/api/documents/{id}/download` | Retrieves the original stored artifact and records an auditable download event | - |

---

## 5. Field Check-Ins `/api/check-ins`
Manages evidence logging and immutable versioning for offline inspections.

Multipart payload JSON includes `commentText`, `deviceTimestamp`, and optional `latitude` / `longitude` values alongside uploaded attachments.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/check-ins` | Lists summary of field evidence check-ins | - |
| `POST` | `/api/check-ins` | Creates a check-in with multiple file attachments | `MultipartFile[] files`, `String payload` (JSON)|
| `GET` | `/api/check-ins/{id}` | Gets full check-in detail and its revision hierarchy | - |
| `PUT` | `/api/check-ins/{id}` | Edits check-in generating a new immutable revision trace | `MultipartFile[] files`, `String payload` (JSON)|
| `GET` | `/api/check-ins/{id}/attachments/{attId}/download`| Streams a specific check-in attachment to client | - |

---

## 6. Critical Actions / Dual Approval `/api/critical-actions`
Facilitates mandatory multi-party approvals (e.g. document destruction, order override). Approval completion requires one quality approver and one finance-or-system-administrator approver.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/critical-actions` | Lists pending critical action requests for the user | - |
| `POST` | `/api/critical-actions` | Initiates a critical action workflow | `CreateCriticalActionRequest`|
| `GET` | `/api/critical-actions/{id}` | Retrieves the specific request metadata and decision status | - |
| `POST` | `/api/critical-actions/{id}/decision` | Decides (APPROVE/REJECT) ensuring dual-party separation | `CriticalActionDecisionRequest`|

---

## 7. System Administration `/api/admin`
Controls user activation/suspension, permission overview, state-transition activation, document-type settings, and managed reason-code catalogs.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/admin/users` | Lists global user configurations and mappings | - |
| `PUT` | `/api/admin/users/{id}` | Activates or suspends a user account | `UpdateUserAccessRequest` |
| `GET` | `/api/admin/permissions` | Lists the currently enforced role-permission matrix | - |
| `GET` | `/api/admin/state-machine` | Exposes the workflow state machine configuration matrix | - |
| `PUT` | `/api/admin/state-machine/{id}` | Activates or deactivates a specific state transition | `UpdateStateMachineTransitionRequest` |
| `GET` | `/api/admin/document-types` | Administration of existing document type definitions | - |
| `PUT` | `/api/admin/document-types/{id}` | Edits settings of a specific documentary type | `UpdateDocumentTypeRequest`|
| `GET` | `/api/admin/reason-codes` | Lists globally configurable reason codes (returns/after-sales) | - |
| `POST` | `/api/admin/reason-codes` | Creates a new system-wide reason code | `CreateReasonCodeRequest` |
| `PUT` | `/api/admin/reason-codes/{id}` | Updates existing reason code (e.g. deactivates it) | `UpdateReasonCodeRequest` |

---

## 8. Meta & Health `/api/meta`, `/api`
System operability checks.

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/health` | Service health status (`{"status": "UP"}`) | - |
| `GET` | `/api/meta/version` | Service version information | - |
