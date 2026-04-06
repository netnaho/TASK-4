package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.CriticalActionRequestRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CriticalActionControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Autowired
    private CriticalActionRequestRepository criticalActionRequestRepository;

    @Test
    void expiredCriticalActionRequestsAreMarkedExpiredOnRead() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-expired", "ORG-ALPHA", "Password!23");
        var finance = createUser(RoleName.FINANCE, "finance-critical-expired", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Approval window elapsed");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now().minusDays(2));
        request.setExpiresAt(OffsetDateTime.now().minusHours(2));
        criticalActionRequestRepository.save(request);

        mockMvc.perform(get("/api/critical-actions").with(authenticated(finance)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("EXPIRED"))
            .andExpect(jsonPath("$.content[0].resolutionNote").value("Expired after 24 hours without two approvals"));
    }

    @Test
    void criticalActionsRequireFinanceAndQualityApprovalsAndPreserveOrderHistory() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-cross-role", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-critical-cross-role", "ORG-ALPHA", "Password!23");
        var qualityPeer = createUser(RoleName.QUALITY_REVIEWER, "quality-critical-peer", "ORG-ALPHA", "Password!23");
        var finance = createUser(RoleName.FINANCE, "finance-critical-cross-role", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Cross-role approval required");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);

        String approveBody = json(java.util.Map.of("decision", "APPROVE", "comments", "Approved"));

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PARTIALLY_APPROVED"));

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(qualityPeer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("CROSS_ROLE_APPROVAL_REQUIRED"));

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(finance))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EXECUTED"))
            .andExpect(jsonPath("$.resolutionNote").value("Quality and finance or system administrator approvals recorded"));

        mockMvc.perform(get("/api/orders/{orderId}", order.getId()).with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELED"))
            .andExpect(jsonPath("$.timeline[?(@.eventType == 'CRITICAL_ACTION_ORDER_CANCELED')].detail").value(org.hamcrest.Matchers.hasItem("Canceled after quality and finance approval")));
    }

    @Test
    void systemAdministratorCanServeAsSecondApprover() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-admin", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-critical-admin", "ORG-ALPHA", "Password!23");
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-critical-admin", "ORG-BETA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("System admin can act as second approver");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);

        String approveBody = json(java.util.Map.of("decision", "APPROVE", "comments", "Approved"));

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PARTIALLY_APPROVED"));

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(approveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    void invalidRequestTypeReturns400() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-invalid-type", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        mockMvc.perform(post("/api/critical-actions")
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "requestType", "INVALID_TYPE",
                    "targetType", "ORDER",
                    "targetId", order.getId(),
                    "justification", "test"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("requestType=INVALID_TYPE"));
    }

    @Test
    void invalidTargetTypeReturns400() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-invalid-target", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        mockMvc.perform(post("/api/critical-actions")
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "requestType", "ORDER_CANCELLATION_AFTER_APPROVAL",
                    "targetType", "BOGUS_TARGET",
                    "targetId", order.getId(),
                    "justification", "test"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("targetType=BOGUS_TARGET"));
    }

    @Test
    void invalidDecisionValueReturns400() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-invalid-decision", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-critical-invalid-decision", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Test invalid decision");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);

        mockMvc.perform(post("/api/critical-actions/{id}/decision", request.getId())
                .with(authenticated(quality))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("decision", "MAYBE", "comments", "test"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details[0]").value("decision=MAYBE"));
    }

    @Test
    void criticalActionListPaginationRespectsScope() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-page", "ORG-ALPHA", "Password!23");
        var otherBuyer = createUser(RoleName.BUYER, "buyer-critical-page-other", "ORG-ALPHA", "Password!23");
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-critical-page", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        // Create 3 requests from buyer, 2 from otherBuyer
        for (int i = 0; i < 3; i++) {
            CriticalActionRequestEntity r = new CriticalActionRequestEntity();
            r.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
            r.setTargetType(CriticalActionTargetType.ORDER);
            r.setTargetId(order.getId());
            r.setJustification("buyer request " + i);
            r.setRequestedBy(buyer);
            r.setStatus(CriticalActionStatus.PENDING);
            r.setCreatedAt(OffsetDateTime.now());
            r.setExpiresAt(OffsetDateTime.now().plusHours(24));
            criticalActionRequestRepository.save(r);
        }
        var otherOrder = createOrder(otherBuyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);
        for (int i = 0; i < 2; i++) {
            CriticalActionRequestEntity r = new CriticalActionRequestEntity();
            r.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
            r.setTargetType(CriticalActionTargetType.ORDER);
            r.setTargetId(otherOrder.getId());
            r.setJustification("other request " + i);
            r.setRequestedBy(otherBuyer);
            r.setStatus(CriticalActionStatus.PENDING);
            r.setCreatedAt(OffsetDateTime.now());
            r.setExpiresAt(OffsetDateTime.now().plusHours(24));
            criticalActionRequestRepository.save(r);
        }

        // Buyer (SELF scope) should see only their own 3 requests — total accurate
        mockMvc.perform(get("/api/critical-actions").with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));

        // Quality (GLOBAL scope) should see all 5 requests
        mockMvc.perform(get("/api/critical-actions").with(authenticated(quality)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void targetScopeControlsCriticalActionVisibility() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-scope", "ORG-ALPHA", "Password!23");
        var requester = createUser(RoleName.FINANCE, "finance-critical-requester", "ORG-BETA", "Password!23");
        var otherFinance = createUser(RoleName.FINANCE, "finance-critical-reader", "ORG-BETA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Scope should follow target order");
        request.setRequestedBy(requester);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(24));
        request = criticalActionRequestRepository.save(request);

        mockMvc.perform(get("/api/critical-actions/{id}", request.getId()).with(authenticated(otherFinance)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.details[0]").value("CRITICAL_ACTION_SCOPE_RESTRICTION"));
    }
}
