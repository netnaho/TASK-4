package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.security.RateLimitFilter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RateLimitIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void clearRateLimitState() {
        rateLimitFilter.clearWindows();
    }

    @Test
    void loginRateLimitedAfter20Requests() throws Exception {
        createUser(RoleName.BUYER, "buyer-rate-limit", "ORG-ALPHA", "Password!23");
        String loginBody = json(Map.of("username", "buyer-rate-limit", "password", "WrongPassword1!"));

        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(APPLICATION_JSON)
                    .content(loginBody))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assert s != 429 : "Request should not be rate limited yet, got 429";
                });
        }

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value(429))
            .andExpect(jsonPath("$.message").value("Rate limit exceeded"))
            .andExpect(jsonPath("$.details[0]").value("MAX_20_LOGIN_REQUESTS_PER_MINUTE"));
    }

    @Test
    void authenticatedEndpointRateLimitedAfter60Requests() throws Exception {
        var user = createUser(RoleName.BUYER, "buyer-rate-auth", "ORG-ALPHA", "Password!23");

        for (int i = 0; i < 60; i++) {
            final int reqNum = i;
            mockMvc.perform(get("/api/orders")
                    .with(authenticated(user)))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assert s != 429 : "Request " + reqNum + " should not be rate limited, got 429";
                });
        }

        mockMvc.perform(get("/api/orders")
                .with(authenticated(user)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.details[0]").value("MAX_60_REQUESTS_PER_MINUTE"));
    }

    @Test
    void rateLimitResponseMatchesApiErrorFormat() throws Exception {
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .with(csrf())
                    .contentType(APPLICATION_JSON)
                    .content(json(Map.of("username", "any", "password", "any"))));
        }

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "any", "password", "any"))))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.details").isArray());
    }
}
