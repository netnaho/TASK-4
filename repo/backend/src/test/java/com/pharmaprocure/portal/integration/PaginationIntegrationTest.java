package com.pharmaprocure.portal.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import org.junit.jupiter.api.Test;

class PaginationIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Test
    void defaultPaginationReturnsFirstPage() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-default", "ORG-ALPHA", "Password!23");
        for (int i = 0; i < 25; i++) {
            createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);
        }

        mockMvc.perform(get("/api/orders").with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.content.length()").value(20))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void explicitPageAndSize() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-explicit", "ORG-ALPHA", "Password!23");
        for (int i = 0; i < 25; i++) {
            createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);
        }

        mockMvc.perform(get("/api/orders")
                .param("page", "1")
                .param("size", "10")
                .with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.totalElements").value(25));
    }

    @Test
    void emptyPageReturnsEmptyContent() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-empty", "ORG-ALPHA", "Password!23");
        createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);

        mockMvc.perform(get("/api/orders")
                .param("page", "99")
                .with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void sortByCreatedAtAsc() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-sort", "ORG-ALPHA", "Password!23");
        createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);
        createOrder(buyer, OrderStatus.APPROVED, 10, 0, 0);

        mockMvc.perform(get("/api/orders")
                .param("sort", "createdAt,asc")
                .with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void statusFilterReturnsOnlyMatchingOrders() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-filter", "ORG-ALPHA", "Password!23");
        createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);
        createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);
        createOrder(buyer, OrderStatus.APPROVED, 10, 0, 0);

        mockMvc.perform(get("/api/orders")
                .param("status", "APPROVED")
                .with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    void scopeRestrictionWithPagination() throws Exception {
        var buyerAlpha = createUser(RoleName.BUYER, "buyer-page-alpha", "ORG-ALPHA", "Password!23");
        var buyerBeta = createUser(RoleName.BUYER, "buyer-page-beta", "ORG-BETA", "Password!23");
        for (int i = 0; i < 5; i++) {
            createOrder(buyerAlpha, OrderStatus.CREATED, 5, 0, 0);
        }
        for (int i = 0; i < 3; i++) {
            createOrder(buyerBeta, OrderStatus.CREATED, 5, 0, 0);
        }

        // BUYER role has SELF scope for orders — each buyer should see only their own
        mockMvc.perform(get("/api/orders").with(authenticated(buyerAlpha)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(5));

        mockMvc.perform(get("/api/orders").with(authenticated(buyerBeta)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void paginationResponseContainsExpectedStructure() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-struct", "ORG-ALPHA", "Password!23");
        createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);

        mockMvc.perform(get("/api/orders").with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").isNumber())
            .andExpect(jsonPath("$.totalPages").isNumber())
            .andExpect(jsonPath("$.number").isNumber())
            .andExpect(jsonPath("$.size").isNumber())
            .andExpect(jsonPath("$.first").isBoolean())
            .andExpect(jsonPath("$.last").isBoolean());
    }

    @Test
    void paginationAndFilterCombined() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-page-combined", "ORG-ALPHA", "Password!23");
        for (int i = 0; i < 15; i++) {
            createOrder(buyer, OrderStatus.APPROVED, 10, 0, 0);
        }
        for (int i = 0; i < 10; i++) {
            createOrder(buyer, OrderStatus.CREATED, 5, 0, 0);
        }

        mockMvc.perform(get("/api/orders")
                .param("status", "APPROVED")
                .param("page", "0")
                .param("size", "10")
                .with(authenticated(buyer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalPages").value(2));
    }
}
