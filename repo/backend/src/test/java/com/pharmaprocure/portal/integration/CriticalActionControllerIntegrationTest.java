package com.pharmaprocure.portal.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.repository.CriticalActionRequestRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CriticalActionControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Autowired
    private CriticalActionRequestRepository criticalActionRequestRepository;

    @Test
    void expiredCriticalActionRequestsAreMarkedExpiredOnRead() throws Exception {
        var buyer = createUser(RoleName.BUYER, "buyer-critical-expired", "ORG-ALPHA", "Password!23");
        var finance = createUser(RoleName.FINANCE, "finance-critical-expired", "ORG-ALPHA", "Password!23");
        var order = createOrder(buyer, com.pharmaprocure.portal.enums.OrderStatus.APPROVED, 5, 0, 0);

        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setRequestType(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL);
        request.setTargetType(CriticalActionTargetType.ORDER);
        request.setTargetId(order.getId());
        request.setJustification("Approval window elapsed");
        request.setRequestedBy(buyer);
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now().minusDays(2));
        request.setExpiresAt(OffsetDateTime.now().minusHours(2));
        criticalActionRequestRepository.save(request);

        mockMvc.perform(get("/api/critical-actions").with(authenticated(finance)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("EXPIRED"))
            .andExpect(jsonPath("$.content[0].resolutionNote").value("Expired after 24 hours without two approvals"));
    }
}
