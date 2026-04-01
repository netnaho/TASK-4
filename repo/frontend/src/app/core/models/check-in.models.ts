export interface CheckInSummaryModel {
  id: number;
  owner: string;
  commentText?: string;
  deviceTimestamp: string;
  serverReceivedAt: string;
  updatedAt: string;
  revisionCount: number;
  hasCoordinates: boolean;
  attachmentCount: number;
}

export interface CheckInAttachmentModel {
  id: number;
  originalFileName: string;
  mimeType: string;
  fileSizeBytes: number;
  sha256Hash: string;
  signatureAlgorithm: string;
  signerKeyId: string;
  contentUrl: string;
  createdAt: string;
}

export interface CheckInRevisionModel {
  id: number;
  revisionNumber: number;
  commentText?: string;
  deviceTimestamp: string;
  latitude?: number;
  longitude?: number;
  changedFields: string[];
  editedBy: string;
  createdAt: string;
}

export interface CheckInAuditModel {
  action: string;
  actor: string;
  detail?: string;
  createdAt: string;
}

export interface CheckInDetailModel {
  id: number;
  owner: string;
  commentText?: string;
  deviceTimestamp: string;
  serverReceivedAt: string;
  latitude?: number;
  longitude?: number;
  attachments: CheckInAttachmentModel[];
  revisions: CheckInRevisionModel[];
  auditEvents: CheckInAuditModel[];
  createdAt: string;
  updatedAt: string;
}
