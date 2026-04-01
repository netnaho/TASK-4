export interface CriticalActionApprovalModel {
  id: number;
  approver: string;
  decision: string;
  comments?: string;
  createdAt: string;
}

export interface CriticalActionAuditModel {
  action: string;
  actor: string;
  detail?: string;
  createdAt: string;
}

export interface CriticalActionRequestModel {
  id: number;
  requestType: string;
  targetType: string;
  targetId: number;
  justification: string;
  requestedBy: string;
  status: string;
  createdAt: string;
  expiresAt: string;
  resolvedAt?: string;
  resolutionNote?: string;
  approvalCount: number;
  approvals: CriticalActionApprovalModel[];
  auditEvents: CriticalActionAuditModel[];
}
