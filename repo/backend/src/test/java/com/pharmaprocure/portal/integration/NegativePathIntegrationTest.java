package com.pharmaprocure.portal.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.RoleName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that protected endpoints return 401 for unauthenticated callers and
 * that resource-fetch endpoints return 404 for non-existent IDs.
 */
class NegativePathIntegrationTest extends AbstractMockMvcIntegrationTest {

    // ── 401 Unauthenticated ──────────────────────────────────────────────────

    @Test
    void ordersEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void documentsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/documents"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void checkInsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/check-ins"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void criticalActionsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/critical-actions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized());
    }

    // ── 404 Not Found ────────────────────────────────────────────────────────

    @Test
    void orderByMissingIdReturns404() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-404-order", "ORG-ALPHA", "Password!23");

        mockMvc.perform(get("/api/orders/{id}", Long.MAX_VALUE).with(authenticated(buyer)))
            .andExpect(status().isNotFound());
    }

    @Test
    void documentByMissingIdReturns404() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-404-doc", "ORG-ALPHA", "Password!23");

        mockMvc.perform(get("/api/documents/{id}", Long.MAX_VALUE).with(authenticated(buyer)))
            .andExpect(status().isNotFound());
    }

    @Test
    void checkInByMissingIdReturns404() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-404-checkin", "ORG-ALPHA", "Password!23");

        mockMvc.perform(get("/api/check-ins/{id}", Long.MAX_VALUE).with(authenticated(buyer)))
            .andExpect(status().isNotFound());
    }

    @Test
    void criticalActionByMissingIdReturns404() throws Exception {
        var quality = createUser(RoleName.QUALITY_REVIEWER, "quality-404-ca", "ORG-ALPHA", "Password!23");

        mockMvc.perform(get("/api/critical-actions/{id}", Long.MAX_VALUE).with(authenticated(quality)))
            .andExpect(status().isNotFound());
    }
}
