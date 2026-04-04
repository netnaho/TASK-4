package com.pharmaprocure.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class CheckInDtos {

    private CheckInDtos() {
    }

    public record CreateCheckInRequest(
        @Size(max = 4000) String commentText,
        OffsetDateTime deviceTimestamp,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        BigDecimal latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        BigDecimal longitude
    ) {
    }

    public record UpdateCheckInRequest(
        @Size(max = 4000) String commentText,
        OffsetDateTime deviceTimestamp,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        BigDecimal latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
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
        Long revisionId,
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
        List<CheckInAttachmentResponse> attachments,
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
