package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.OrderStateMachineDefinitionEntity;
import com.pharmaprocure.portal.enums.RoleName;
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
                .content(json(java.util.Map.of("active", false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(buyer.getId()))
            .andExpect(jsonPath("$.active").value(false));
    }
}
