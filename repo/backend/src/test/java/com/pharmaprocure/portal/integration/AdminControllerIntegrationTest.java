package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.OrderStateMachineDefinitionEntity;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdminControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Test
    void adminCanUpdateStateMachineTransition() throws Exception {
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-state-machine", "ORG-ALPHA", "Password!23");
        OrderStateMachineDefinitionEntity transition = orderStateMachineDefinitionRepository.findByActiveTrue().stream()
            .filter(item -> "PICK_PACK".equals(item.getFromStatus()) && "SHIPPED".equals(item.getToStatus()))
            .findFirst()
            .orElseThrow();

        mockMvc.perform(put("/api/admin/state-machine/{id}", transition.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(java.util.Map.of("active", false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(transition.getId()))
            .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/admin/state-machine").with(authenticated(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(String.format("$.transitions[?(@.id == %s)].active", transition.getId())).value(org.hamcrest.Matchers.hasItem(false)));
    }

    @Test
    void adminCanSuspendUserAccess() throws Exception {
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-user-access", "ORG-ALPHA", "Password!23");
        var buyer = createUser(RoleName.BUYER, "buyer-user-access", "ORG-ALPHA", "Password!23");

        mockMvc.perform(put("/api/admin/users/{id}", buyer.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("active", false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(buyer.getId()))
            .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void adminCanResetUserPasswordWithValidPolicy() throws Exception {
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-pw-reset-ok", "ORG-ALPHA", "Password!23");
        var buyer = createUser(RoleName.BUYER, "buyer-pw-reset-ok", "ORG-ALPHA", "OldPassword!23");

        mockMvc.perform(put("/api/admin/users/{id}/password", buyer.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("newPassword", "NewSecure@Pass1"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(buyer.getId()));
    }

    @Test
    void adminPasswordResetRejectsTooShortPassword() throws Exception {
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-pw-short", "ORG-ALPHA", "Password!23");
        var buyer = createUser(RoleName.BUYER, "buyer-pw-short", "ORG-ALPHA", "OldPassword!23");

        mockMvc.perform(put("/api/admin/users/{id}/password", buyer.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("newPassword", "Short1!"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Password policy violation"));
    }

    @Test
    void adminPasswordResetRejectsWeakCharacterClassPassword() throws Exception {
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-pw-weak", "ORG-ALPHA", "Password!23");
        var buyer = createUser(RoleName.BUYER, "buyer-pw-weak", "ORG-ALPHA", "OldPassword!23");

        // Only lowercase — fails the 3-class requirement
        mockMvc.perform(put("/api/admin/users/{id}/password", buyer.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("newPassword", "allloercaselongpass"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Password policy violation"));
    }

    @Test
    void nonAdminCannotResetPassword() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-pw-unauthorized", "ORG-ALPHA", "Password!23");
        var target = createUser(RoleName.BUYER, "buyer-pw-target", "ORG-ALPHA", "OldPassword!23");

        mockMvc.perform(put("/api/admin/users/{id}/password", target.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("newPassword", "NewSecure@Pass1"))))
            .andExpect(status().isForbidden());
    }
}
