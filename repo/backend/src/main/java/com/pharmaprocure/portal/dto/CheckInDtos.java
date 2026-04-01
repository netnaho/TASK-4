package com.pharmaprocure.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class CheckInDtos {

    private CheckInDtos() {
    }

    public record CreateCheckInRequest(
        String commentText,
        @NotNull OffsetDateTime deviceTimestamp,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
    }

    public record UpdateCheckInRequest(
        String commentText,
        @NotNull OffsetDateTime deviceTimestamp,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
    }

    public record CheckInSummaryResponse(
        Long id,
        String owner,
        String commentText,
        OffsetDateTime deviceTimestamp,
        OffsetDateTime serverReceivedAt,
        OffsetDateTime updatedAt,
        int revisionCount,
        boolean hasCoordinates,
        int attachmentCount
    ) {
    }

    public record CheckInAttachmentResponse(
        Long id,
        String originalFileName,
        String mimeType,
        Long fileSizeBytes,
        String sha256Hash,
        String signatureAlgorithm,
        String signerKeyId,
        String contentUrl,
        OffsetDateTime createdAt
    ) {
    }

    public record CheckInRevisionResponse(
        Long id,
        int revisionNumber,
        String commentText,
        OffsetDateTime deviceTimestamp,
        BigDecimal latitude,
        BigDecimal longitude,
        List<String> changedFields,
        String editedBy,
        OffsetDateTime createdAt
    ) {
    }

    public record CheckInAuditResponse(
        String action,
        String actor,
        String detail,
        OffsetDateTime createdAt
    ) {
    }

    public record CheckInDetailResponse(
        Long id,
        String owner,
        String commentText,
        OffsetDateTime deviceTimestamp,
        OffsetDateTime serverReceivedAt,
        BigDecimal latitude,
        BigDecimal longitude,
        List<CheckInAttachmentResponse> attachments,
        List<CheckInRevisionResponse> revisions,
        List<CheckInAuditResponse> auditEvents,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {
    }
}
