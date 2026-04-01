package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OrderControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Test
    void organizationScopedFinanceCannotAccessDifferentOrganizationOrder() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-order-scope", "ORG-ALPHA", "Password!23");
        var finance = createUser(RoleName.FINANCE, "finance-order-scope", "ORG-BETA", "Password!23");
        var order = createOrder(buyer, OrderStatus.CREATED, 10, 0, 0);

        mockMvc.perform(get("/api/orders/{orderId}", order.getId()).with(authenticated(finance)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.details[0]").value("ORDER_SCOPE_RESTRICTION"));
    }

    @Test
    void partialShipmentStaysOpenAndAllowsReceiptBeforeFinalShipment() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-partial-flow", "ORG-ALPHA", "Password!23");
        var fulfillment = createUser(RoleName.FULFILLMENT_CLERK, "fulfillment-partial-flow", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.PICK_PACK, 10, 0, 0);
        Long orderItemId = order.getItems().get(0).getId();

        mockMvc.perform(post("/api/orders/{orderId}/shipments", order.getId())
                .with(authenticated(fulfillment))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "notes", "First carton",
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 4))
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PARTIALLY_SHIPPED"))
            .andExpect(jsonPath("$.items[0].shippedQuantity").value(4))
            .andExpect(jsonPath("$.items[0].remainingToShip").value(6));

        mockMvc.perform(post("/api/orders/{orderId}/receipts", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "notes", "Received first carton",
                    "discrepancyConfirmed", false,
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 4))
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PARTIALLY_SHIPPED"))
            .andExpect(jsonPath("$.items[0].receivedQuantity").value(4))
            .andExpect(jsonPath("$.items[0].remainingToReceive").value(0));

        mockMvc.perform(post("/api/orders/{orderId}/shipments", order.getId())
                .with(authenticated(fulfillment))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "notes", "Final carton",
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 6))
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SHIPPED"))
            .andExpect(jsonPath("$.items[0].shippedQuantity").value(10))
            .andExpect(jsonPath("$.items[0].remainingToShip").value(0));
    }
}
