package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.RoleName;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Test
    void loginRejectsMissingCsrfToken() throws Exception {
        createUser(RoleName.BUYER, "buyer-auth-no-csrf", "ORG-ALPHA", "Password!23");

        mockMvc.perform(post("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "buyer-auth-no-csrf", "password", "Password!23"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void loginReturnsOrganizationCodeOnSuccess() throws Exception {
        createUser(RoleName.BUYER, "buyer-auth-success", "ORG-ALPHA", "Password!23");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "buyer-auth-success", "password", "Password!23"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.organizationCode").value("ORG-ALPHA"))
            .andExpect(jsonPath("$.user.role").value("BUYER"));
    }

    @Test
    void loginRejectsLockedAccounts() throws Exception {
        var user = createUser(RoleName.BUYER, "buyer-auth-locked", "ORG-ALPHA", "Password!23");
        user.setLockoutUntil(OffsetDateTime.now().plusMinutes(15));
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("username", "buyer-auth-locked", "password", "Password!23"))))
            .andExpect(status().isLocked())
            .andExpect(jsonPath("$.code").value(423))
            .andExpect(jsonPath("$.message").value("Account locked"));
    }
}
