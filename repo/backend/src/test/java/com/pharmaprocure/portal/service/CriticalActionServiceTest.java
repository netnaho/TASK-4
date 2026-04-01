package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pharmaprocure.portal.dto.CriticalActionDtos.CreateCriticalActionRequest;
import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.entity.ProcurementOrderEntity;
import com.pharmaprocure.portal.entity.RoleEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.CriticalActionApprovalRepository;
import com.pharmaprocure.portal.repository.CriticalActionAuditEventRepository;
import com.pharmaprocure.portal.repository.CriticalActionRequestRepository;
import com.pharmaprocure.portal.repository.DocumentRepository;
import com.pharmaprocure.portal.repository.ProcurementOrderRepository;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import com.pharmaprocure.portal.security.UserPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CriticalActionServiceTest {

    private final CriticalActionRequestRepository requestRepository = Mockito.mock(CriticalActionRequestRepository.class);
    private final CriticalActionApprovalRepository approvalRepository = Mockito.mock(CriticalActionApprovalRepository.class);
    private final CriticalActionAuditEventRepository auditRepository = Mockito.mock(CriticalActionAuditEventRepository.class);
    private final CurrentUserService currentUserService = Mockito.mock(CurrentUserService.class);
    private final CriticalActionAuditService auditService = Mockito.mock(CriticalActionAuditService.class);
    private final ProcurementOrderRepository orderRepository = Mockito.mock(ProcurementOrderRepository.class);
    private final DocumentRepository documentRepository = Mockito.mock(DocumentRepository.class);
    private final PermissionAuthorizationService permissionAuthorizationService = Mockito.mock(PermissionAuthorizationService.class);

    private final CriticalActionService service = new CriticalActionService(
        requestRepository,
        approvalRepository,
        auditRepository,
        currentUserService,
        auditService,
        orderRepository,
        documentRepository,
        permissionAuthorizationService
    );

    @Test
    void createsOrderCriticalActionRequest() {
        UserEntity buyer = buyer();
        ProcurementOrderEntity order = new ProcurementOrderEntity();
        order.setId(5L);
        order.setBuyer(buyer);
        order.setCurrentStatus(OrderStatus.APPROVED);
        when(currentUserService.requireCurrentUser()).thenReturn(buyer);
        when(permissionAuthorizationService.canAccessResource(buyer, Permission.CRITICAL_ACTION_REQUEST, buyer.getId(), RoleName.BUYER, buyer.getOrganizationCode())).thenReturn(true);
        when(orderRepository.findWithItemsById(5L)).thenReturn(Optional.of(order));
        when(requestRepository.findByRequestTypeAndTargetTypeAndTargetIdAndStatusIn(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(requestRepository.save(any(CriticalActionRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(approvalRepository.findByRequestIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(auditRepository.findByRequestIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        var response = service.create(new CreateCriticalActionRequest("ORDER_CANCELLATION_AFTER_APPROVAL", "ORDER", 5L, "Need cancellation"));
        assertEquals(CriticalActionStatus.PENDING.name(), response.status());
        assertEquals(CriticalActionRequestType.ORDER_CANCELLATION_AFTER_APPROVAL.name(), response.requestType());
    }

    @Test
    void expiresPendingRequestAfterDeadline() {
        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setId(9L);
        request.setRequestType(CriticalActionRequestType.RETENTION_OVERRIDE);
        request.setTargetType(CriticalActionTargetType.DOCUMENT);
        request.setTargetId(3L);
        request.setRequestedBy(buyer());
        request.setJustification("Expired");
        request.setStatus(CriticalActionStatus.PARTIALLY_APPROVED);
        request.setCreatedAt(OffsetDateTime.now().minusDays(2));
        request.setExpiresAt(OffsetDateTime.now().minusHours(1));
        UserEntity buyer = buyer();
        when(currentUserService.requireCurrentUser()).thenReturn(buyer);
        when(requestRepository.findById(9L)).thenReturn(Optional.of(request));
        when(permissionAuthorizationService.canAccessResource(buyer, Permission.CRITICAL_ACTION_VIEW, request.getRequestedBy().getId(), request.getRequestedBy().getRole().getName(), request.getRequestedBy().getOrganizationCode())).thenReturn(true);
        when(approvalRepository.findByRequestIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(auditRepository.findByRequestIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        var response = service.get(9L, principal(buyer));
        assertEquals(CriticalActionStatus.EXPIRED.name(), response.status());
    }

    @Test
    void blocksOutOfScopeRequestRetrieval() {
        UserEntity buyer = buyer();
        UserEntity admin = user(2L, RoleName.SYSTEM_ADMINISTRATOR, "admin1", "Admin One");
        CriticalActionRequestEntity request = new CriticalActionRequestEntity();
        request.setId(10L);
        request.setRequestType(CriticalActionRequestType.DOCUMENT_DESTRUCTION);
        request.setTargetType(CriticalActionTargetType.DOCUMENT);
        request.setTargetId(22L);
        request.setRequestedBy(admin);
        request.setJustification("Admin request");
        request.setStatus(CriticalActionStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setExpiresAt(OffsetDateTime.now().plusHours(4));

        when(currentUserService.requireCurrentUser()).thenReturn(buyer);
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(permissionAuthorizationService.canAccessResource(buyer, Permission.CRITICAL_ACTION_VIEW, admin.getId(), RoleName.SYSTEM_ADMINISTRATOR, admin.getOrganizationCode())).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> service.get(10L, principal(buyer)));
        assertEquals(403, exception.getCode());
    }

    @Test
    void rejectsDuplicateActiveRequests() {
        UserEntity buyer = buyer();
        ProcurementOrderEntity order = new ProcurementOrderEntity();
        order.setId(5L);
        order.setBuyer(buyer);
        order.setCurrentStatus(OrderStatus.APPROVED);
        CriticalActionRequestEntity existing = new CriticalActionRequestEntity();
        existing.setId(88L);
        when(currentUserService.requireCurrentUser()).thenReturn(buyer);
        when(permissionAuthorizationService.canAccessResource(buyer, Permission.CRITICAL_ACTION_REQUEST, buyer.getId(), RoleName.BUYER, buyer.getOrganizationCode())).thenReturn(true);
        when(orderRepository.findWithItemsById(5L)).thenReturn(Optional.of(order));
        when(requestRepository.findByRequestTypeAndTargetTypeAndTargetIdAndStatusIn(any(), any(), any(), any())).thenReturn(Optional.of(existing));

        assertThrows(ApiException.class, () -> service.create(new CreateCriticalActionRequest("ORDER_CANCELLATION_AFTER_APPROVAL", "ORDER", 5L, "Need cancellation")));
    }

    private UserEntity buyer() {
        return user(1L, RoleName.BUYER, "buyer1", "Buyer One");
    }

    private UserEntity user(Long id, RoleName roleName, String username, String displayName) {
        RoleEntity role = new RoleEntity();
        role.setName(roleName);
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setRole(role);
        user.setDisplayName(displayName);
        user.setUsername(username);
        user.setOrganizationCode("ORG-ALPHA");
        return user;
    }

    private UserPrincipal principal(UserEntity user) {
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getOrganizationCode(),
            "hash",
            user.getRole().getName(),
            java.util.Set.of(),
            true
        );
    }
}
