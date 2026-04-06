package com.pharmaprocure.portal.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.RoleName;
import org.junit.jupiter.api.Test;

class SecurityHeadersIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Test
    void publicEndpointCarriesSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    void authenticatedEndpointCarriesSecurityHeaders() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-headers", "ORG-ALPHA", "Password!23");

        mockMvc.perform(get("/api/orders").with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    void contentSecurityPolicyDirectivesAreCorrect() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(header().string("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:; font-src 'self'; frame-ancestors 'none'"));
    }
}
