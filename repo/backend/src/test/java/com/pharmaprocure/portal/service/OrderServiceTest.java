package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pharmaprocure.portal.audit.AuditService;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.RecordPaymentRequest;
import com.pharmaprocure.portal.entity.ProcurementOrderEntity;
import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.AfterSalesCaseRepository;
import com.pharmaprocure.portal.repository.OrderPaymentRepository;
import com.pharmaprocure.portal.repository.OrderReviewRepository;
import com.pharmaprocure.portal.repository.OrderReturnRepository;
import com.pharmaprocure.portal.repository.OrderStatusHistoryRepository;
import com.pharmaprocure.portal.repository.ProcurementOrderRepository;
import com.pharmaprocure.portal.repository.ProductCatalogRepository;
import com.pharmaprocure.portal.repository.ReasonCodeRepository;
import com.pharmaprocure.portal.repository.ReceiptRepository;
import com.pharmaprocure.portal.repository.ShipmentRepository;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

class OrderServiceTest {

    private final ProcurementOrderRepository orderRepository = Mockito.mock(ProcurementOrderRepository.class);
    private final ProductCatalogRepository productCatalogRepository = Mockito.mock(ProductCatalogRepository.class);
    private final OrderReviewRepository orderReviewRepository = Mockito.mock(OrderReviewRepository.class);
    private final OrderPaymentRepository orderPaymentRepository = Mockito.mock(OrderPaymentRepository.class);
    private final ShipmentRepository shipmentRepository = Mockito.mock(ShipmentRepository.class);
    private final ReceiptRepository receiptRepository = Mockito.mock(ReceiptRepository.class);
    private final ReasonCodeRepository reasonCodeRepository = Mockito.mock(ReasonCodeRepository.class);
    private final OrderReturnRepository orderReturnRepository = Mockito.mock(OrderReturnRepository.class);
    private final AfterSalesCaseRepository afterSalesCaseRepository = Mockito.mock(AfterSalesCaseRepository.class);
    private final OrderStatusHistoryRepository orderStatusHistoryRepository = Mockito.mock(OrderStatusHistoryRepository.class);
    private final OrderStateMachineService orderStateMachineService = Mockito.mock(OrderStateMachineService.class);
    private final CurrentUserService currentUserService = Mockito.mock(CurrentUserService.class);
    private final AuditService auditService = Mockito.mock(AuditService.class);
    private final OrderQuantityService orderQuantityService = Mockito.mock(OrderQuantityService.class);
    private final PermissionAuthorizationService permissionAuthorizationService = Mockito.mock(PermissionAuthorizationService.class);

    private final OrderService service = new OrderService(
        orderRepository,
        productCatalogRepository,
        orderReviewRepository,
        orderPaymentRepository,
        shipmentRepository,
        receiptRepository,
        reasonCodeRepository,
        orderReturnRepository,
        afterSalesCaseRepository,
        orderStatusHistoryRepository,
        orderStateMachineService,
        currentUserService,
        auditService,
        orderQuantityService,
        permissionAuthorizationService
    );

    @Test
    void mapsConcurrentDuplicatePaymentConstraintToApiException() {
        UserEntity buyer = user(1L, RoleName.BUYER, "buyer-order-race");
        UserEntity finance = user(2L, RoleName.FINANCE, "finance-order-race");
        ProcurementOrderEntity order = new ProcurementOrderEntity();
        order.setId(10L);
        order.setOrderNumber("ORD-RACE");
        order.setBuyer(buyer);
        order.setCurrentStatus(OrderStatus.APPROVED);
        order.setPaymentRecorded(false);

        when(orderRepository.findWithItemsById(10L)).thenReturn(Optional.of(order));
        when(currentUserService.requireCurrentUser()).thenReturn(finance);
        when(permissionAuthorizationService.canAccessResource(finance, Permission.ORDER_PAYMENT_RECORD, buyer.getId(), RoleName.BUYER, buyer.getOrganizationCode())).thenReturn(true);
        when(orderPaymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        doNothing().when(orderStateMachineService).validateTransition(OrderStatus.APPROVED, OrderStatus.PAYMENT_RECORDED);
        when(orderStatusHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderPaymentRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

        ApiException exception = assertThrows(ApiException.class, () -> service.recordPayment(10L, new RecordPaymentRequest("PAY-RACE", BigDecimal.TEN)));
        assertEquals(400, exception.getCode());
        assertEquals("PAYMENT_ALREADY_RECORDED", exception.getDetails().get(0));
    }

    private UserEntity user(Long id, RoleName roleName, String username) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(username);
        user.setOrganizationCode("ORG-ALPHA");
        user.setRole(role);
        return user;
    }
}
