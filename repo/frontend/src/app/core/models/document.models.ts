export interface DocumentTypeModel {
  id: number;
  code: string;
  name: string;
  description: string;
  evidenceAllowed: boolean;
  active: boolean;
}

export interface DocumentTemplateModel {
  id: number;
  documentTypeId: number;
  documentTypeCode: string;
  templateName: string;
  templateBody: string;
  active: boolean;
}

export interface DocumentSummaryModel {
  id: number;
  documentNumber?: string;
  title: string;
  documentTypeCode: string;
  status: string;
  owner: string;
  metadataTags?: string;
  updatedAt: string;
}

export interface DocumentVersionModel {
  id: number;
  versionNumber: number;
  titleSnapshot: string;
  contentText?: string;
  originalFileName?: string;
  mimeType?: string;
  fileSizeBytes?: number;
  sha256Hash?: string;
  createdAt: string;
  createdBy: string;
}

export interface DocumentApprovalStepModel {
  id: number;
  stepOrder: number;
  approverRole: string;
  approverUser?: string;
  status: string;
  comments?: string;
  actedAt?: string;
}

export interface DocumentArchiveModel {
  id: number;
  archiveHash: string;
  signatureAlgorithm: string;
  signerKeyId: string;
  archivedAt: string;
  archivedBy: string;
}

export interface DocumentAuditModel {
  action: string;
  actor: string;
  detail?: string;
  createdAt: string;
}

export interface DocumentDetailModel {
  id: number;
  documentNumber?: string;
  title: string;
  documentTypeCode: string;
  status: string;
  owner: string;
  metadataTags?: string;
  versions: DocumentVersionModel[];
  approvalSteps: DocumentApprovalStepModel[];
  archive?: DocumentArchiveModel;
  auditEvents: DocumentAuditModel[];
  createdAt: string;
  updatedAt: string;
}

export interface DocumentPreviewModel {
  documentId: number;
  versionId: number;
  documentNumber: string;
  mimeType: string;
  title: string;
  previewUrl: string;
  watermarkText: string;
  previewSupported: boolean;
}
