package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.DocumentApprovalStepEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DocumentApprovalStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentApprovalWorkflowService {

    public DocumentApprovalStepEntity nextPendingStep(List<DocumentApprovalStepEntity> steps) {
        return steps.stream()
            .filter(step -> step.getStatus() == DocumentApprovalStatus.PENDING)
            .min(Comparator.comparingInt(DocumentApprovalStepEntity::getStepOrder))
            .orElse(null);
    }

    public void approveStep(DocumentApprovalStepEntity step, UserEntity actor, String comments) {
        if (step.getStatus() != DocumentApprovalStatus.PENDING) {
            throw new ApiException(400, "Approval step is not pending", List.of("stepId=" + step.getId(), "status=" + step.getStatus().name()));
        }
        String requiredRole = normalizeRole(step.getApproverRole());
        String actorRole = actor.getRole().getName().name();
        if (!requiredRole.equals(actorRole)) {
            throw new ApiException(403, "Access denied", List.of("DOCUMENT_APPROVER_ROLE_MISMATCH", "requiredRole=" + requiredRole, "actorRole=" + actorRole));
        }
        step.setStatus(DocumentApprovalStatus.APPROVED);
        step.setApproverUser(actor);
        step.setComments(comments);
        step.setActedAt(OffsetDateTime.now());
    }

    public boolean allApproved(List<DocumentApprovalStepEntity> steps) {
        return steps.stream().allMatch(step -> step.getStatus() == DocumentApprovalStatus.APPROVED);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new ApiException(400, "Approval workflow is misconfigured", List.of("MISSING_APPROVER_ROLE"));
        }
        try {
            return RoleName.valueOf(role.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new ApiException(400, "Approval workflow is misconfigured", List.of("INVALID_APPROVER_ROLE=" + role));
        }
    }
}
