package com.pharmaprocure.portal.integration;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.RoleName;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the MaskingUtils → GlobalExceptionHandler sanitization pipeline.
 * These tests verify that sensitive tokens (emails, raw usernames) do not appear verbatim
 * in API error responses.
 */
class SanitizationIntegrationTest extends AbstractMockMvcIntegrationTest {

    /**
     * A failed login where the username looks like an email must not echo the email
     * address back in the response body.
     */
    @Test
    void loginErrorDoesNotLeakEmailInResponse() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "attacker@evil.com", "password", "wrong"))))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(not(containsString("attacker@evil.com"))));
    }

    /**
     * A failed login must not expose the raw username in the response detail.
     * The response details should contain a generic code, not the caller-supplied value.
     */
    @Test
    void loginErrorResponseContainsGenericCode() throws Exception {
        createUser(RoleName.BUYER, "buyer-sanit-generic", "ORG-ALPHA", "Password!23");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "buyer-sanit-generic", "password", "WrongPass1!"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("Invalid credentials"))
            .andExpect(jsonPath("$.details[0]").value("INVALID_CREDENTIALS"))
            .andExpect(content().string(not(containsString("buyer-sanit-generic"))));
    }

    /**
     * Bean-validation errors are passed through sanitizeText() in GlobalExceptionHandler.
     * This test verifies the validation error response structure is intact after sanitization.
     */
    @Test
    void validationErrorResponseIsSanitizedAndWellFormed() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-sanit-val", "ORG-ALPHA", "Password!23");

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .with(authenticated(buyer))
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details").isArray());
    }

    /**
     * A request with an email-like value in the username field to a non-existent user
     * must not leak the email in any part of the error body.
     */
    @Test
    void emailInRequestDoesNotLeakIntoErrorBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "probe@internal.corp", "password", "any"))))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(not(containsString("probe@internal.corp"))))
            .andExpect(content().string(not(containsString("internal.corp"))));
    }
}
