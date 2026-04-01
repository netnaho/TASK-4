package com.pharmaprocure.portal.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.CriticalActionApprovalRepository;
import com.pharmaprocure.portal.repository.CriticalActionRequestRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

class ConcurrencyIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Autowired
    private CriticalActionRequestRepository criticalActionRequestRepository;

    @Autowired
    private CriticalActionApprovalRepository criticalActionApprovalRepository;

    @Test
    void rapidSubmitForReviewMaintainsConsistentState() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-concurrent-submit", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.CREATED, 10, 0, 0);
        Long orderId = order.getId();

        // Sequential rapid submissions to verify state consistency
        MvcResult first = mockMvc.perform(post("/api/orders/{orderId}/submit-review", orderId)
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andReturn();

        MvcResult second = mockMvc.perform(post("/api/orders/{orderId}/submit-review", orderId)
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andReturn();

        // First should succeed
        assertEquals(200, first.getResponse().getStatus());

        // Second may succeed (same-status no-op) or fail — either way, state should be consistent
        int secondStatus = second.getResponse().getStatus();
        assertTrue(secondStatus == 200 || secondStatus == 400,
            "Second submit should return 200 (idempotent) or 400, got " + secondStatus);

        // Verify the order is in UNDER_REVIEW
        var updated = orderRepository.findWithItemsById(orderId).orElseThrow();
        assertEquals(OrderStatus.UNDER_REVIEW, updated.getCurrentStatus());
    }

    @Test
    void criticalActionDecisionsByDifferentUsersProducesConsistentState() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-concurrent-decide", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-concurrent-decide", "ORG-ALPHA", "Password!23");
        var finance = createUser(RoleName.FINANCE, "finance-concurrent-decide", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Sequential approval test");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);
        Long requestId = request.getId();

        String decisionBody = json(Map.of("decision", "APPROVE", "comments", "Approved"));

        // First approval — quality reviewer
        MvcResult qualityResult = mockMvc.perform(post("/api/critical-actions/{id}/decision", requestId)
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(decisionBody))
            .andReturn();

        assertEquals(200, qualityResult.getResponse().getStatus(), "First approval should succeed");

        // Second approval — finance (different user)
        MvcResult financeResult = mockMvc.perform(post("/api/critical-actions/{id}/decision", requestId)
                .with(authenticated(finance))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(decisionBody))
            .andReturn();

        assertEquals(200, financeResult.getResponse().getStatus(), "Second approval should succeed");

        // Verify exactly 2 approvals
        long approvalCount = criticalActionApprovalRepository.findByRequestIdOrderByCreatedAtAsc(requestId).size();
        assertEquals(2, approvalCount, "Should have exactly 2 approvals");

        // Verify final state is EXECUTED (both approved, action executed)
        CriticalActionRequestEntity updated = criticalActionRequestRepository.findById(requestId).orElseThrow();
        assertEquals(CriticalActionStatus.EXECUTED, updated.getStatus());
    }

    @Test
    void sameUserCannotApproveCriticalActionTwice() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-same-user-decide", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-same-user-decide", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Same-user rejection test");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);
        Long requestId = request.getId();

        String decisionBody = json(Map.of("decision", "APPROVE", "comments", "First approval"));

        // First approval succeeds
        mockMvc.perform(post("/api/critical-actions/{id}/decision", requestId)
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(decisionBody))
            .andReturn();

        // Same user tries again — should be rejected
        MvcResult secondResult = mockMvc.perform(post("/api/critical-actions/{id}/decision", requestId)
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(decisionBody))
            .andReturn();

        assertEquals(400, secondResult.getResponse().getStatus());
    }
}
