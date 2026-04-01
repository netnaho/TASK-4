package com.pharmaprocure.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record DocumentTypeResponse(Long id, String code, String name, String description, boolean evidenceAllowed, boolean active) {
    }

    public record DocumentTemplateResponse(Long id, Long documentTypeId, String documentTypeCode, String templateName, String templateBody, boolean active) {
    }

    public record CreateDocumentTemplateRequest(
        @NotNull Long documentTypeId,
        @NotBlank String templateName,
        String templateBody,
        boolean active
    ) {
    }

    public record CreateDocumentDraftRequest(
        @NotNull Long documentTypeId,
        Long templateId,
        @NotBlank String title,
        String contentText,
        String metadataTags,
        @NotEmpty List<String> approvalRoles
    ) {
    }

    public record UpdateDocumentDraftRequest(
        @NotBlank String title,
        String contentText,
        String metadataTags
    ) {
    }

    public record ApproveDocumentRequest(String comments) {
    }

    public record DocumentSummaryResponse(
        Long id,
        String documentNumber,
        String title,
        String documentTypeCode,
        String status,
        String owner,
        String metadataTags,
        OffsetDateTime updatedAt
    ) {
    }

    public record DocumentVersionResponse(
        Long id,
        int versionNumber,
        String titleSnapshot,
        String contentText,
        String originalFileName,
        String mimeType,
        Long fileSizeBytes,
        String sha256Hash,
        OffsetDateTime createdAt,
        String createdBy
    ) {
    }

    public record DocumentApprovalStepResponse(
        Long id,
        int stepOrder,
        String approverRole,
        String approverUser,
        String status,
        String comments,
        OffsetDateTime actedAt
    ) {
    }

    public record DocumentArchiveResponse(
        Long id,
        String archiveHash,
        String signatureAlgorithm,
        String signerKeyId,
        OffsetDateTime archivedAt,
        String archivedBy
    ) {
    }

    public record DocumentAuditResponse(String action, String actor, String detail, OffsetDateTime createdAt) {
    }

    public record DocumentDetailResponse(
        Long id,
        String documentNumber,
        String title,
        String documentTypeCode,
        String status,
        String owner,
        String metadataTags,
        List<DocumentVersionResponse> versions,
        List<DocumentApprovalStepResponse> approvalSteps,
        DocumentArchiveResponse archive,
        List<DocumentAuditResponse> auditEvents,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {
    }

    public record DocumentPreviewResponse(
        Long documentId,
        Long versionId,
        String documentNumber,
        String mimeType,
        String title,
        String previewUrl,
        String watermarkText,
        boolean previewSupported
    ) {
    }
}
