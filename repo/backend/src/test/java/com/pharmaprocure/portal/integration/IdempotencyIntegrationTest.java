package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.CriticalActionRequestRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class IdempotencyIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Autowired
    private CriticalActionRequestRepository criticalActionRequestRepository;

    @Test
    void doubleSubmitForReviewDoesNotCorruptState() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-idem-submit", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.CREATED, 10, 0, 0);

        mockMvc.perform(post("/api/orders/{orderId}/submit-review", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        // Second submit — the transition method treats same-status transitions as no-ops
        // The order stays in UNDER_REVIEW without error
        mockMvc.perform(post("/api/orders/{orderId}/submit-review", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }

    @Test
    void doubleApproveOrderDoesNotCorruptState() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-idem-approve", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-idem-approve", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.UNDER_REVIEW, 10, 0, 0);

        String approveBody = json(Map.of("decision", "APPROVED", "comments", "Looks good"));

        mockMvc.perform(post("/api/orders/{orderId}/approve", order.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));

        // Second approval — same-status transition, treated as no-op
        mockMvc.perform(post("/api/orders/{orderId}/approve", order.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void doubleCriticalActionDecisionBySameUserReturns400() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-idem-critical", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-idem-critical", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Idempotency test");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);

        String decisionBody = json(Map.of("decision", "APPROVE", "comments", "Approved"));

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(decisionBody))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(decisionBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("SAME_USER_REJECTION"));
    }

    @Test
    void doublePaymentRecordingSecondIsRejected() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-idem-payment", "ORG-ALPHA", "Password!23");
        var finance = createUser(RoleName.FINANCE, "finance-idem-payment", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.APPROVED, 10, 0, 0);

        String paymentBody = json(Map.of("referenceNumber", "PAY-001", "amount", 125.00));

        mockMvc.perform(post("/api/orders/{orderId}/record-payment", order.getId())
                .with(authenticated(finance))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(paymentBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAYMENT_RECORDED"));

        // Second payment recording fails — the system rejects duplicate payment records
        int secondStatus = mockMvc.perform(post("/api/orders/{orderId}/record-payment", order.getId())
                .with(authenticated(finance))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(paymentBody))
            .andReturn().getResponse().getStatus();

        // Should fail with either 400 (business rule) or 500 (constraint violation)
        org.junit.jupiter.api.Assertions.assertTrue(
            secondStatus == 400 || secondStatus == 500,
            "Second payment should be rejected, got " + secondStatus
        );
    }

    @Test
    void doublePickPackDoesNotCorruptState() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-idem-pickpack", "ORG-ALPHA", "Password!23");
        var fulfillment = createUser(RoleName.FULFILLMENT_CLERK, "fulfillment-idem-pickpack", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.PAYMENT_RECORDED, 10, 0, 0);

        mockMvc.perform(post("/api/orders/{orderId}/pick-pack", order.getId())
                .with(authenticated(fulfillment))
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PICK_PACK"));

        // Second pick-pack — same-status transition
        mockMvc.perform(post("/api/orders/{orderId}/pick-pack", order.getId())
                .with(authenticated(fulfillment))
                .with(csrf())
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PICK_PACK"));
    }
}
