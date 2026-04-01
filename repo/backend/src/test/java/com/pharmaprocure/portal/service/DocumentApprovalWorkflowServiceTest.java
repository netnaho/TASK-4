package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pharmaprocure.portal.entity.DocumentApprovalStepEntity;
import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DocumentApprovalStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentApprovalWorkflowServiceTest {

    private final DocumentApprovalWorkflowService service = new DocumentApprovalWorkflowService();

    @Test
    void progressesApprovalToNextPendingStep() {
        DocumentApprovalStepEntity first = new DocumentApprovalStepEntity();
        first.setStepOrder(1);
        first.setApproverRole(RoleName.QUALITY_REVIEWER.name());
        first.setStatus(DocumentApprovalStatus.PENDING);
        DocumentApprovalStepEntity second = new DocumentApprovalStepEntity();
        second.setStepOrder(2);
        second.setStatus(DocumentApprovalStatus.PENDING);

        service.approveStep(first, user(RoleName.QUALITY_REVIEWER), "Approved");

        assertEquals(DocumentApprovalStatus.APPROVED, first.getStatus());
        assertEquals(second, service.nextPendingStep(List.of(first, second)));
    }

    @Test
    void identifiesWhenAllStepsAreApproved() {
        DocumentApprovalStepEntity first = new DocumentApprovalStepEntity();
        first.setStatus(DocumentApprovalStatus.APPROVED);
        DocumentApprovalStepEntity second = new DocumentApprovalStepEntity();
        second.setStatus(DocumentApprovalStatus.APPROVED);
        assertTrue(service.allApproved(List.of(first, second)));
    }

    @Test
    void rejectsApprovalWhenActorRoleDoesNotMatchConfiguredStepRole() {
        DocumentApprovalStepEntity step = new DocumentApprovalStepEntity();
        step.setId(10L);
        step.setStepOrder(1);
        step.setApproverRole(RoleName.QUALITY_REVIEWER.name());
        step.setStatus(DocumentApprovalStatus.PENDING);

        assertThrows(ApiException.class, () -> service.approveStep(step, user(RoleName.SYSTEM_ADMINISTRATOR), "Bypass attempt"));
    }

    @Test
    void rejectsStandardUserForFinanceRoutedStep() {
        DocumentApprovalStepEntity step = new DocumentApprovalStepEntity();
        step.setId(11L);
        step.setStepOrder(1);
        step.setApproverRole(RoleName.FINANCE.name());
        step.setStatus(DocumentApprovalStatus.PENDING);

        assertThrows(ApiException.class, () -> service.approveStep(step, user(RoleName.BUYER), "Buyer cannot approve finance step"));
    }

    private UserEntity user(RoleName roleName) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        UserEntity user = new UserEntity();
        user.setRole(role);
        user.setUsername(roleName.name().toLowerCase());
        user.setDisplayName(roleName.name());
        return user;
    }
}
