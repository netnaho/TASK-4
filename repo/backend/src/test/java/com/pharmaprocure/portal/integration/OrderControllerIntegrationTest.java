package com.pharmaprocure.portal.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.ReasonCodeEntity;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OrderControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private com.pharmaprocure.portal.repository.ReasonCodeRepository reasonCodeRepository;

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
                    "discrepancyConfirmed", true,
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

    @Test
    void shipmentOrderMismatchRequiresDiscrepancyConfirmation() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-shipment-order-mismatch", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.PARTIALLY_SHIPPED, 10, 4, 0);
        Long orderItemId = order.getItems().get(0).getId();

        mockMvc.perform(post("/api/orders/{orderId}/receipts", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "notes", "Received shipped partial quantity",
                    "discrepancyConfirmed", false,
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 4))
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Receipt discrepancy confirmation required"));
    }

    @Test
    void shortReceiptRequiresDiscrepancyConfirmation() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-short-receipt", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.SHIPPED, 10, 10, 0);
        Long orderItemId = order.getItems().get(0).getId();

        mockMvc.perform(post("/api/orders/{orderId}/receipts", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "notes", "Only part of the shipment arrived",
                    "discrepancyConfirmed", false,
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 6))
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Receipt discrepancy confirmation required"));

        mockMvc.perform(post("/api/orders/{orderId}/receipts", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "notes", "Only part of the shipment arrived",
                    "discrepancyConfirmed", true,
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 6, "discrepancyReason", "4 units missing"))
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receipts[0].hasDiscrepancy").value(true))
            .andExpect(jsonPath("$.items[0].discrepancyFlag").value(true));
    }

    @Test
    void returnsAndAfterSalesRequireActiveManagedReasonCodes() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-reason-code-enforcement", "ORG-ALPHA", "Password!23");
        var admin = createUser(RoleName.SYSTEM_ADMINISTRATOR, "admin-reason-code-enforcement", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, OrderStatus.RECEIVED, 10, 10, 10);
        Long orderItemId = order.getItems().get(0).getId();

        ReasonCodeEntity returnReason = ensureReasonCode("RETURN", "DAMAGED_GOODS", "Damaged goods");
        ReasonCodeEntity afterSalesReason = ensureReasonCode("AFTER_SALES", "DAMAGED_GOODS", "Damaged goods");

        mockMvc.perform(put("/api/admin/reason-codes/{id}", returnReason.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("label", returnReason.getLabel(), "active", false))))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/reason-codes/{id}", afterSalesReason.getId())
                .with(authenticated(admin))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of("label", afterSalesReason.getLabel(), "active", false))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{orderId}/returns", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "reasonCode", "DAMAGED_GOODS",
                    "comments", "Inactive reason should fail",
                    "items", List.of(Map.of("orderItemId", orderItemId, "quantity", 1))
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Reason code is not active for this workflow"));

        mockMvc.perform(post("/api/orders/{orderId}/after-sales-cases", order.getId())
                .with(authenticated(buyer))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(json(Map.of(
                    "orderItemId", orderItemId,
                    "reasonCode", "DAMAGED_GOODS",
                    "structuredDetail", "Inactive reason should fail"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Reason code is not active for this workflow"));

        mockMvc.perform(get("/api/orders/reason-codes").param("codeType", "RETURN").with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code == 'DAMAGED_GOODS')]").isEmpty());
    }

    private ReasonCodeEntity ensureReasonCode(String codeType, String code, String label) {
        return reasonCodeRepository.findByCodeTypeOrderByLabelAsc(codeType).stream()
            .filter(reason -> code.equals(reason.getCode()))
            .findFirst()
            .orElseGet(() -> {
                ReasonCodeEntity entity = new ReasonCodeEntity();
                entity.setCodeType(codeType);
                entity.setCode(code);
                entity.setLabel(label);
                entity.setActive(true);
                return reasonCodeRepository.save(entity);
            });
    }
}
