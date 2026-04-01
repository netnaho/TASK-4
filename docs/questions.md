# questions.md

## Business Logic Questions Log

### 1. Order cancellation after approval
**Question:** The prompt says order cancellation after approval requires dual approval, but it does not specify what happens if fulfillment has already started.  
**My Understanding/Hypothesis:** If picking, packing, or shipping has already started, cancellation should be blocked and the user should use return or discrepancy workflows instead.  
**Solution:** Allow cancellation only before shipment begins; after shipment, handle issues through returns or after-sales cases.

### 2. Partial shipment and receipt discrepancies
**Question:** The prompt supports partial shipment and partial receipt, but does not clearly define how quantity mismatches should affect order completion.  
**My Understanding/Hypothesis:** If received quantity is less than shipped quantity, the system should flag a discrepancy and keep the order open until resolved.  
**Solution:** Add discrepancy records and keep the order in a partially received or exception state until the remaining quantity is received, returned, or written off.

### 3. After-sales cases and order completion
**Question:** The prompt mentions damaged goods and temperature excursion as after-sales cases, but does not say whether the original order can still be marked completed.  
**My Understanding/Hypothesis:** The order can be operationally completed once receipt is done, while after-sales cases remain open separately for traceability.  
**Solution:** Mark the order as completed after final receipt, but keep linked after-sales cases open until resolved.

### 4. Dual approval expiration behavior
**Question:** The prompt says dual approval expires after 24 hours if not completed, but does not define what happens to the pending request afterward.  
**My Understanding/Hypothesis:** Expired requests should automatically move to an expired state and require re-submission.  
**Solution:** Add a dual-approval request entity with statuses such as pending, approved, expired, and rejected.

### 5. Document numbering timing
**Question:** The prompt says numbering resets every 01/01 by document type and year, but does not specify whether drafts consume a number or only controlled documents do.  
**My Understanding/Hypothesis:** Only controlled or finalized documents should consume a sequence number to avoid unnecessary gaps.  
**Solution:** Generate document numbers when a document enters the official controlled workflow, not during early draft save.

### 6. Document destruction handling
**Question:** The prompt requires dual approval for document destruction, but does not specify whether destroyed documents are fully removed from the system.  
**My Understanding/Hypothesis:** Because of audit and compliance needs, documents should not be physically deleted.  
**Solution:** Use soft delete or destruction markers while preserving metadata and immutable audit logs.

### 7. Check-in record edit behavior
**Question:** The prompt says check-in edits must be highlighted with a full revision trail, but does not clarify whether the original record can be overwritten.  
**My Understanding/Hypothesis:** Original values should remain preserved for compliance and traceability.  
**Solution:** Use versioned updates so edits create a new revision while retaining the original record and all prior versions.

### 8. Multi-file upload validation
**Question:** The prompt defines allowed file types and size limits, but does not specify what happens when one file fails in a multi-file upload.  
**My Understanding/Hypothesis:** Invalid files should be rejected individually without blocking valid files.  
**Solution:** Validate each file independently, accept valid files, reject invalid ones with clear feedback, and log failed attempts.

### 9. Returned quantity accounting
**Question:** The prompt includes returns with reason codes, but does not define whether returned quantities reduce the original receipt total or create a separate adjustment flow.  
**My Understanding/Hypothesis:** Returns should be tracked separately so original receipt records remain immutable.  
**Solution:** Keep shipment and receipt records unchanged and create linked return records that adjust net fulfilled quantities in reporting.

### 10. Data visibility by role and scope
**Question:** The prompt mentions RBAC and data-scope rules, but does not define whether users can see all orders or only scoped records.  
**My Understanding/Hypothesis:** Buyers should only see their own facility’s orders, while operational roles see records relevant to their assigned scope.  
**Solution:** Implement role + scope-based filtering (such as facility, warehouse, or department) at both UI and API levels.

### 11. Payment status and fulfillment dependency
**Question:** The prompt includes a payment recorded status, but does not explicitly state whether picking and shipping can begin before payment is recorded.  
**My Understanding/Hypothesis:** Fulfillment should normally begin only after approval and payment confirmation unless configured otherwise by policy.  
**Solution:** Make payment a configurable fulfillment prerequisite, with a default rule that pick/pack cannot start until payment is recorded.

### 12. Audit logging scope
**Question:** The prompt requires audit events for create, update, approve, and download actions, but does not specify whether failed or denied actions should also be logged.  
**My Understanding/Hypothesis:** Failed or unauthorized attempts are important for security and compliance review.  
**Solution:** Log both successful and failed sensitive actions, including denied approvals, failed uploads, and blocked access attempts.