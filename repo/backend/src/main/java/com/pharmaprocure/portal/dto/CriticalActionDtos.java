package com.pharmaprocure.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public final class CriticalActionDtos {

    private CriticalActionDtos() {
    }

    public record CreateCriticalActionRequest(
        @NotBlank String requestType,
        @NotBlank String targetType,
        @NotNull Long targetId,
        @NotBlank String justification
    ) {
    }

    public record CriticalActionDecisionRequest(
        @NotBlank String decision,
        String comments
    ) {
    }

    public record CriticalActionApprovalResponse(
        Long id,
        String approver,
        String decision,
        String comments,
        OffsetDateTime createdAt
    ) {
    }

    public record CriticalActionAuditResponse(
        String action,
        String actor,
        String detail,
        OffsetDateTime createdAt
    ) {
    }

    public record CriticalActionRequestResponse(
        Long id,
        String requestType,
        String targetType,
        Long targetId,
        String justification,
        String requestedBy,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        OffsetDateTime resolvedAt,
        String resolutionNote,
        int approvalCount,
        List<CriticalActionApprovalResponse> approvals,
        List<CriticalActionAuditResponse> auditEvents
    ) {
    }
}
