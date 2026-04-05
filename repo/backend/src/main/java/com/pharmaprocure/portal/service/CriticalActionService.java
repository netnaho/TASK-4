package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.CriticalActionDtos.CreateCriticalActionRequest;
import com.pharmaprocure.portal.dto.CriticalActionDtos.CriticalActionApprovalResponse;
import com.pharmaprocure.portal.dto.CriticalActionDtos.CriticalActionAuditResponse;
import com.pharmaprocure.portal.dto.CriticalActionDtos.CriticalActionRequestResponse;
import com.pharmaprocure.portal.entity.CriticalActionApprovalEntity;
import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.entity.DocumentEntity;
import com.pharmaprocure.portal.entity.OrderStatusHistoryEntity;
import com.pharmaprocure.portal.entity.ProcurementOrderEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.CriticalActionAuditAction;
import com.pharmaprocure.portal.enums.CriticalActionDecision;
import com.pharmaprocure.portal.enums.CriticalActionRequestType;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.enums.CriticalActionTargetType;
import com.pharmaprocure.portal.enums.DocumentStatus;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.CriticalActionApprovalRepository;
import com.pharmaprocure.portal.repository.CriticalActionAuditEventRepository;
import com.pharmaprocure.portal.repository.CriticalActionRequestRepository;
import com.pharmaprocure.portal.repository.DocumentRepository;
import com.pharmaprocure.portal.repository.OrderStatusHistoryRepository;
import com.pharmaprocure.portal.repository.ProcurementOrderRepository;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import com.pharmaprocure.portal.security.UserPrincipal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriticalActionService {

    private final CriticalActionRequestRepository requestRepository;
    private final CriticalActionApprovalRepository approvalRepository;
    private final CriticalActionAuditEventRepository auditRepository;
    private final CurrentUserService currentUserService;
    private final CriticalActionAuditService auditService;
    private final ProcurementOrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final DocumentRepository documentRepository;
    private final PermissionAuthorizationService permissionAuthorizationService;

    public CriticalActionService(
        CriticalActionRequestRepository requestRepository,
        CriticalActionApprovalRepository approvalRepository,
        CriticalActionAuditEventRepository auditRepository,
        CurrentUserService currentUserService,
        CriticalActionAuditService auditService,
        ProcurementOrderRepository orderRepository,
        OrderStatusHistoryRepository orderStatusHistoryRepository,
        DocumentRepository documentRepository,
        PermissionAuthorizationService permissionAuthorizationService
    ) {
        this.requestRepository = requestRepository;
        this.approvalRepository = approvalRepository;
        this.auditRepository = auditRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.documentRepository = documentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public Page<CriticalActionRequestResponse> list(Pageable pageable, CriticalActionStatus statusFilter) {
        UserEntity actor = currentUserService.requireCurrentUser();
        permissionAuthorizationService.requireDataScope(actor, Permission.CRITICAL_ACTION_VIEW);
        Page<CriticalActionRequestEntity> requests = requestRepository.findAll(pageable);
        List<CriticalActionRequestResponse> visible = requests.getContent().stream()
            .filter(request -> statusFilter == null || request.getStatus() == statusFilter)
            .filter(request -> canAccessTarget(actor, request, Permission.CRITICAL_ACTION_VIEW))
            .map(request -> {
            expireIfNeeded(request);
            return toResponse(request);
        }).toList();
        return new PageImpl<>(visible, pageable, visible.size());
    }

    @Transactional
    public CriticalActionRequestResponse create(CreateCriticalActionRequest request) {
        UserEntity actor = currentUserService.requireCurrentUser();
        CriticalActionRequestType requestType = CriticalActionRequestType.valueOf(request.requestType());
        CriticalActionTargetType targetType = CriticalActionTargetType.valueOf(request.targetType());
        validateTarget(requestType, targetType, request.targetId(), actor);
        requestRepository.findByRequestTypeAndTargetTypeAndTargetIdAndStatusIn(requestType, targetType, request.targetId(), List.of(CriticalActionStatus.PENDING, CriticalActionStatus.PARTIALLY_APPROVED))
            .ifPresent(existing -> { throw new ApiException(400, "Active critical action request already exists", List.of("requestId=" + existing.getId())); });

        CriticalActionRequestEntity entity = new CriticalActionRequestEntity();
        entity.setRequestType(requestType);
        entity.setTargetType(targetType);
        entity.setTargetId(request.targetId());
        entity.setJustification(request.justification());
        entity.setRequestedBy(actor);
        entity.setStatus(CriticalActionStatus.PENDING);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setExpiresAt(OffsetDateTime.now().plusHours(24));
        entity = requestRepository.save(entity);
        auditService.record(entity, actor, CriticalActionAuditAction.REQUESTED, request.justification());
        return toResponse(entity);
    }

    @Transactional
    public CriticalActionRequestResponse decide(Long requestId, CriticalActionDecision decision, String comments) {
        UserEntity actor = currentUserService.requireCurrentUser();
        CriticalActionRequestEntity request = requireRequest(requestId, actor, Permission.CRITICAL_ACTION_VIEW);
        requireCrossRoleApprover(actor);
        expireIfNeeded(request);
        if (request.getStatus() == CriticalActionStatus.EXPIRED) {
            throw new ApiException(400, "Critical action request has expired", List.of("requestId=" + requestId));
        }
        if (request.getStatus() == CriticalActionStatus.EXECUTED || request.getStatus() == CriticalActionStatus.REJECTED) {
            throw new ApiException(400, "Critical action request is already resolved", List.of(request.getStatus().name()));
        }
        List<CriticalActionApprovalEntity> existingApprovals = approvalRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
        if (existingApprovals.stream().anyMatch(approval -> approval.getApproverUser().getId().equals(actor.getId()))) {
            throw new ApiException(400, "The same user cannot approve twice", List.of("SAME_USER_REJECTION"));
        }
        if (decision == CriticalActionDecision.APPROVE
            && existingApprovals.stream().anyMatch(approval -> approval.getDecision() == CriticalActionDecision.APPROVE && approvalLane(approval.getApproverUser()) == approvalLane(actor))) {
            throw new ApiException(400, "Critical actions require one quality approval and one finance or system administrator approval", List.of("CROSS_ROLE_APPROVAL_REQUIRED"));
        }

        CriticalActionApprovalEntity approval = new CriticalActionApprovalEntity();
        approval.setRequest(request);
        approval.setApproverUser(actor);
        approval.setDecision(decision);
        approval.setComments(comments);
        approval.setCreatedAt(OffsetDateTime.now());
        approvalRepository.save(approval);
        List<CriticalActionApprovalEntity> approvals = new ArrayList<>(existingApprovals);
        approvals.add(approval);

        if (decision == CriticalActionDecision.REJECT) {
            request.setStatus(CriticalActionStatus.REJECTED);
            request.setResolvedAt(OffsetDateTime.now());
            request.setResolutionNote(comments == null ? "Rejected" : comments);
            auditService.record(request, actor, CriticalActionAuditAction.REJECTED, comments);
            return toResponse(request);
        }

        if (hasRequiredApprovals(approvals)) {
            request.setStatus(CriticalActionStatus.APPROVED);
            request.setResolvedAt(OffsetDateTime.now());
            request.setResolutionNote("Quality and finance or system administrator approvals recorded");
            auditService.record(request, actor, CriticalActionAuditAction.APPROVED, "Cross-role approval completed");
            execute(request, actor);
        } else {
            request.setStatus(CriticalActionStatus.PARTIALLY_APPROVED);
            auditService.record(request, actor, CriticalActionAuditAction.APPROVED, actor.getRole().getName().name() + " approval recorded");
        }
        return toResponse(request);
    }

    @Transactional
    public CriticalActionRequestResponse get(Long requestId, UserPrincipal principal) {
        UserEntity actor = currentUserService.requireCurrentUser();
        if (!actor.getId().equals(principal.id())) {
            throw new ApiException(403, "Access denied", List.of("AUTH_CONTEXT_MISMATCH"));
        }
        CriticalActionRequestEntity request = requireRequest(requestId, actor, Permission.CRITICAL_ACTION_VIEW);
        expireIfNeeded(request);
        return toResponse(request);
    }

    private void execute(CriticalActionRequestEntity request, UserEntity actor) {
        switch (request.getRequestType()) {
            case ORDER_CANCELLATION_AFTER_APPROVAL -> executeOrderCancellation(request, actor);
            case DOCUMENT_DESTRUCTION -> executeDocumentDestruction(request, actor);
            case RETENTION_OVERRIDE -> executeRetentionOverride(request, actor);
        }
        request.setStatus(CriticalActionStatus.EXECUTED);
        request.setResolvedAt(OffsetDateTime.now());
        auditService.record(request, actor, CriticalActionAuditAction.EXECUTED, request.getRequestType().name());
    }

    private void executeOrderCancellation(CriticalActionRequestEntity request, UserEntity actor) {
        ProcurementOrderEntity order = orderRepository.findWithItemsById(request.getTargetId())
            .orElseThrow(() -> new ApiException(404, "Order not found", List.of("orderId=" + request.getTargetId())));
        OrderStatus fromStatus = order.getCurrentStatus();
        order.setCurrentStatus(OrderStatus.CANCELED);
        order.setUpdatedAt(OffsetDateTime.now());
        appendOrderHistory(order, actor, fromStatus, OrderStatus.CANCELED, "CRITICAL_ACTION_ORDER_CANCELED", "Canceled after quality and finance approval");
    }

    private void executeDocumentDestruction(CriticalActionRequestEntity request, UserEntity actor) {
        DocumentEntity document = documentRepository.findWithCurrentVersionById(request.getTargetId())
            .orElseThrow(() -> new ApiException(404, "Document not found", List.of("documentId=" + request.getTargetId())));
        document.setStatus(DocumentStatus.DESTROYED);
        document.setDestroyedAt(OffsetDateTime.now());
        document.setDestroyedBy(actor);
        document.setUpdatedAt(OffsetDateTime.now());
    }

    private void executeRetentionOverride(CriticalActionRequestEntity request, UserEntity actor) {
        DocumentEntity document = documentRepository.findWithCurrentVersionById(request.getTargetId())
            .orElseThrow(() -> new ApiException(404, "Document not found", List.of("documentId=" + request.getTargetId())));
        document.setRetentionOverrideUntil(OffsetDateTime.now().plusDays(365));
        document.setUpdatedAt(OffsetDateTime.now());
    }

    private void validateTarget(CriticalActionRequestType requestType, CriticalActionTargetType targetType, Long targetId, UserEntity actor) {
        switch (requestType) {
            case ORDER_CANCELLATION_AFTER_APPROVAL -> {
                if (targetType != CriticalActionTargetType.ORDER) {
                    throw new ApiException(400, "Invalid target type for order cancellation", List.of(targetType.name()));
                }
                ProcurementOrderEntity order = orderRepository.findWithItemsById(targetId)
                    .orElseThrow(() -> new ApiException(404, "Order not found", List.of("orderId=" + targetId)));
                boolean allowed = permissionAuthorizationService.canAccessResource(
                    actor,
                    Permission.CRITICAL_ACTION_REQUEST,
                    order.getBuyer().getId(),
                    order.getBuyer().getRole().getName(),
                    order.getBuyer().getOrganizationCode()
                );
                if (!allowed) {
                    throw new ApiException(403, "Access denied", List.of("ORDER_SCOPE_RESTRICTION"));
                }
                if (order.getCurrentStatus() != OrderStatus.APPROVED && order.getCurrentStatus() != OrderStatus.PAYMENT_RECORDED && order.getCurrentStatus() != OrderStatus.PICK_PACK && order.getCurrentStatus() != OrderStatus.PARTIALLY_SHIPPED && order.getCurrentStatus() != OrderStatus.SHIPPED) {
                    throw new ApiException(400, "Only approved or fulfilled orders require critical cancellation", List.of(order.getCurrentStatus().name()));
                }
            }
            case DOCUMENT_DESTRUCTION, RETENTION_OVERRIDE -> {
                if (targetType != CriticalActionTargetType.DOCUMENT) {
                    throw new ApiException(400, "Invalid target type for document action", List.of(targetType.name()));
                }
                DocumentEntity document = documentRepository.findWithCurrentVersionById(targetId)
                    .orElseThrow(() -> new ApiException(404, "Document not found", List.of("documentId=" + targetId)));
                boolean allowed = permissionAuthorizationService.canAccessResource(
                    actor,
                    Permission.CRITICAL_ACTION_REQUEST,
                    document.getOwnerUser().getId(),
                    document.getOwnerUser().getRole().getName(),
                    document.getOwnerUser().getOrganizationCode()
                );
                if (!allowed) {
                    throw new ApiException(403, "Access denied", List.of("DOCUMENT_SCOPE_RESTRICTION"));
                }
            }
        }
    }

    private CriticalActionRequestEntity requireRequest(Long requestId, UserEntity actor, Permission permission) {
        CriticalActionRequestEntity request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ApiException(404, "Critical action request not found", List.of("requestId=" + requestId)));
        if (!canAccessTarget(actor, request, permission)) {
            throw new ApiException(403, "Access denied", List.of("CRITICAL_ACTION_SCOPE_RESTRICTION", "requestId=" + requestId));
        }
        return request;
    }

    private void expireIfNeeded(CriticalActionRequestEntity request) {
        if ((request.getStatus() == CriticalActionStatus.PENDING || request.getStatus() == CriticalActionStatus.PARTIALLY_APPROVED) && request.getExpiresAt().isBefore(OffsetDateTime.now())) {
            request.setStatus(CriticalActionStatus.EXPIRED);
            request.setResolvedAt(OffsetDateTime.now());
            request.setResolutionNote("Expired after 24 hours without two approvals");
            auditService.record(request, request.getRequestedBy(), CriticalActionAuditAction.EXPIRED, request.getResolutionNote());
        }
    }

    private void requireCrossRoleApprover(UserEntity actor) {
        RoleName roleName = actor.getRole().getName();
        if (approvalLane(actor) == null) {
            throw new ApiException(403, "Critical actions require quality and finance or system administrator segregation", List.of("CROSS_ROLE_APPROVER_REQUIRED"));
        }
    }

    private boolean hasRequiredApprovals(List<CriticalActionApprovalEntity> approvals) {
        boolean qualityApproved = approvals.stream().anyMatch(approval -> approval.getDecision() == CriticalActionDecision.APPROVE && "QUALITY".equals(approvalLane(approval.getApproverUser())));
        boolean financeOrAdminApproved = approvals.stream().anyMatch(approval -> approval.getDecision() == CriticalActionDecision.APPROVE && "FINANCE_ADMIN".equals(approvalLane(approval.getApproverUser())));
        return qualityApproved && financeOrAdminApproved;
    }

    private boolean canAccessTarget(UserEntity actor, CriticalActionRequestEntity request, Permission permission) {
        UserEntity requester = request.getRequestedBy();
        if (!permissionAuthorizationService.canAccessResource(actor, permission, requester.getId(), requester.getRole().getName(), requester.getOrganizationCode())) {
            return false;
        }
        return switch (request.getTargetType()) {
            case ORDER -> orderRepository.findWithItemsById(request.getTargetId())
                .map(order -> permissionAuthorizationService.canAccessResource(actor, permission, order.getBuyer().getId(), order.getBuyer().getRole().getName(), order.getBuyer().getOrganizationCode()))
                .orElse(false);
            case DOCUMENT -> documentRepository.findWithCurrentVersionById(request.getTargetId())
                .map(document -> permissionAuthorizationService.canAccessResource(actor, permission, document.getOwnerUser().getId(), document.getOwnerUser().getRole().getName(), document.getOwnerUser().getOrganizationCode()))
                .orElse(false);
        };
    }

    private String approvalLane(UserEntity actor) {
        return switch (actor.getRole().getName()) {
            case QUALITY_REVIEWER -> "QUALITY";
            case FINANCE, SYSTEM_ADMINISTRATOR -> "FINANCE_ADMIN";
            default -> null;
        };
    }

    private void appendOrderHistory(ProcurementOrderEntity order, UserEntity actor, OrderStatus from, OrderStatus to, String eventType, String detail) {
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrder(order);
        history.setActorUser(actor);
        history.setFromStatus(from == null ? null : from.name());
        history.setToStatus(to.name());
        history.setEventType(eventType);
        history.setDetail(detail);
        history.setCreatedAt(OffsetDateTime.now());
        orderStatusHistoryRepository.save(history);
    }

    private CriticalActionRequestResponse toResponse(CriticalActionRequestEntity request) {
        List<CriticalActionApprovalEntity> approvals = approvalRepository.findByRequestIdOrderByCreatedAtAsc(request.getId());
        return new CriticalActionRequestResponse(
            request.getId(),
            request.getRequestType().name(),
            request.getTargetType().name(),
            request.getTargetId(),
            request.getJustification(),
            request.getRequestedBy().getDisplayName(),
            request.getStatus().name(),
            request.getCreatedAt(),
            request.getExpiresAt(),
            request.getResolvedAt(),
            request.getResolutionNote(),
            approvals.size(),
            approvals.stream().map(item -> new CriticalActionApprovalResponse(item.getId(), item.getApproverUser().getDisplayName(), item.getDecision().name(), item.getComments(), item.getCreatedAt())).toList(),
            auditRepository.findByRequestIdOrderByCreatedAtAsc(request.getId()).stream().map(item -> new CriticalActionAuditResponse(item.getAction().name(), item.getActorUser().getDisplayName(), item.getDetail(), item.getCreatedAt())).toList()
        );
    }
}
