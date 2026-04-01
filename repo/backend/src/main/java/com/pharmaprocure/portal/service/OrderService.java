package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.audit.AuditService;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.AfterSalesCaseCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.AfterSalesCaseResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.CreateOrderRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.OrderDetailResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.OrderItemResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.OrderSummaryResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.PaymentResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.QuantityRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReceiptCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReceiptItemResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReceiptResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.RecordPaymentRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReturnCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReturnItemResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReturnResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReviewOrderRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReviewResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ShipmentCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ShipmentItemResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ShipmentResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.TraceabilityEventResponse;
import com.pharmaprocure.portal.entity.AfterSalesCaseEntity;
import com.pharmaprocure.portal.entity.OrderPaymentEntity;
import com.pharmaprocure.portal.entity.OrderReviewEntity;
import com.pharmaprocure.portal.entity.OrderReturnEntity;
import com.pharmaprocure.portal.entity.OrderStatusHistoryEntity;
import com.pharmaprocure.portal.entity.ProcurementOrderEntity;
import com.pharmaprocure.portal.entity.ProcurementOrderItemEntity;
import com.pharmaprocure.portal.entity.ProductCatalogEntity;
import com.pharmaprocure.portal.entity.ReceiptEntity;
import com.pharmaprocure.portal.entity.ReceiptItemEntity;
import com.pharmaprocure.portal.entity.ReturnItemEntity;
import com.pharmaprocure.portal.entity.ShipmentEntity;
import com.pharmaprocure.portal.entity.ShipmentItemEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.enums.ReviewDecision;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.AfterSalesCaseRepository;
import com.pharmaprocure.portal.repository.OrderPaymentRepository;
import com.pharmaprocure.portal.repository.OrderReviewRepository;
import com.pharmaprocure.portal.repository.OrderReturnRepository;
import com.pharmaprocure.portal.repository.OrderStatusHistoryRepository;
import com.pharmaprocure.portal.repository.ProcurementOrderRepository;
import com.pharmaprocure.portal.repository.ProductCatalogRepository;
import com.pharmaprocure.portal.repository.ReceiptRepository;
import com.pharmaprocure.portal.repository.ShipmentRepository;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final DateTimeFormatter NUMBER_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProcurementOrderRepository orderRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final OrderReviewRepository orderReviewRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final ReceiptRepository receiptRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final AfterSalesCaseRepository afterSalesCaseRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderStateMachineService stateMachineService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final OrderQuantityService orderQuantityService;
    private final PermissionAuthorizationService permissionAuthorizationService;

    public OrderService(
        ProcurementOrderRepository orderRepository,
        ProductCatalogRepository productCatalogRepository,
        OrderReviewRepository orderReviewRepository,
        OrderPaymentRepository orderPaymentRepository,
        ShipmentRepository shipmentRepository,
        ReceiptRepository receiptRepository,
        OrderReturnRepository orderReturnRepository,
        AfterSalesCaseRepository afterSalesCaseRepository,
        OrderStatusHistoryRepository orderStatusHistoryRepository,
        OrderStateMachineService stateMachineService,
        CurrentUserService currentUserService,
        AuditService auditService,
        OrderQuantityService orderQuantityService,
        PermissionAuthorizationService permissionAuthorizationService
    ) {
        this.orderRepository = orderRepository;
        this.productCatalogRepository = productCatalogRepository;
        this.orderReviewRepository = orderReviewRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.receiptRepository = receiptRepository;
        this.orderReturnRepository = orderReturnRepository;
        this.afterSalesCaseRepository = afterSalesCaseRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.stateMachineService = stateMachineService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.orderQuantityService = orderQuantityService;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        UserEntity buyer = currentUserService.requireCurrentUser();
        ProcurementOrderEntity order = new ProcurementOrderEntity();
        OffsetDateTime now = OffsetDateTime.now();
        order.setOrderNumber(generateNumber("ORD"));
        order.setBuyer(buyer);
        order.setCurrentStatus(OrderStatus.CREATED);
        order.setReviewRequired(true);
        order.setPaymentRecorded(false);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setNotes(request.notes());

        for (var itemRequest : request.items()) {
            ProductCatalogEntity product = productCatalogRepository.findById(itemRequest.productId())
                .orElseThrow(() -> new ApiException(404, "Product not found", List.of("productId=" + itemRequest.productId())));
            ProcurementOrderItemEntity item = new ProcurementOrderItemEntity();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductNameSnapshot(product.getName());
            item.setSkuSnapshot(product.getSku());
            item.setUnitSnapshot(product.getUnit());
            item.setUnitPriceSnapshot(product.getUnitPrice());
            item.setOrderedQuantity(itemRequest.quantity());
            item.setShippedQuantity(0);
            item.setReceivedQuantity(0);
            item.setReturnedQuantity(0);
            item.setDamagedQuantity(0);
            item.setDiscrepancyFlag(false);
            order.getItems().add(item);
        }

        ProcurementOrderEntity saved = orderRepository.save(order);
        appendHistory(saved, buyer, null, OrderStatus.CREATED, "ORDER_CREATED", "Order created by buyer");
        auditService.record("ORDER_CREATED", buyer.getUsername(), saved.getOrderNumber());
        return getOrder(saved.getId());
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listOrders(Pageable pageable, OrderStatus statusFilter) {
        UserEntity actor = currentUserService.requireCurrentUser();
        DataScope scope = permissionAuthorizationService.requireDataScope(actor, Permission.ORDER_VIEW);
        Long buyerId = (scope == DataScope.SELF) ? actor.getId() : null;
        String orgCode = (scope == DataScope.ORGANIZATION) ? actor.getOrganizationCode() : null;
        com.pharmaprocure.portal.enums.RoleName roleName = (scope == DataScope.TEAM) ? actor.getRole().getName() : null;
        Page<ProcurementOrderEntity> orders = orderRepository.findFiltered(buyerId, orgCode, roleName, statusFilter, pageable);
        return orders.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long orderId) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_VIEW);
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse submitForReview(Long orderId) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_CREATE);
        transition(order, OrderStatus.UNDER_REVIEW, "SUBMITTED_FOR_REVIEW", "Submitted for review");
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse cancelOrder(Long orderId) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_CREATE);
        if (order.getCurrentStatus() == OrderStatus.APPROVED || order.getCurrentStatus() == OrderStatus.PAYMENT_RECORDED || order.getCurrentStatus() == OrderStatus.PICK_PACK || order.getCurrentStatus() == OrderStatus.PARTIALLY_SHIPPED || order.getCurrentStatus() == OrderStatus.SHIPPED || order.getCurrentStatus() == OrderStatus.RECEIVED || order.getCurrentStatus() == OrderStatus.RETURNED) {
            throw new ApiException(400, "Approved or fulfilled orders cannot be canceled directly", List.of("Dual approval will be required in a later phase"));
        }
        OrderStatus from = order.getCurrentStatus();
        if (from == OrderStatus.CREATED) {
            order.setCurrentStatus(OrderStatus.CANCELED);
            order.setUpdatedAt(OffsetDateTime.now());
            appendHistory(order, currentUserService.requireCurrentUser(), from, OrderStatus.CANCELED, "ORDER_CANCELED", "Canceled before review");
            return toDetail(order);
        }
        transition(order, OrderStatus.CANCELED, "ORDER_CANCELED", "Canceled before approval");
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse review(Long orderId, ReviewOrderRequest request) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_APPROVE);
        UserEntity reviewer = currentUserService.requireCurrentUser();
        ReviewDecision decision = ReviewDecision.valueOf(request.decision().toUpperCase());
        if (decision == ReviewDecision.REJECTED) {
            throw new ApiException(400, "Rejected orders are not supported in this phase", List.of("Use cancellation from under review if required"));
        }
        transition(order, OrderStatus.APPROVED, "ORDER_APPROVED", request.comments());
        OrderReviewEntity review = orderReviewRepository.findByOrderId(order.getId()).orElseGet(OrderReviewEntity::new);
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setDecision(decision);
        review.setComments(request.comments());
        review.setReviewedAt(OffsetDateTime.now());
        orderReviewRepository.save(review);
        order.setApprovedAt(review.getReviewedAt());
        order.setReviewCompletedAt(review.getReviewedAt());
        auditService.record("ORDER_APPROVED", reviewer.getUsername(), order.getOrderNumber());
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse recordPayment(Long orderId, RecordPaymentRequest request) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_PAYMENT_RECORD);
        UserEntity financeUser = currentUserService.requireCurrentUser();
        transition(order, OrderStatus.PAYMENT_RECORDED, "PAYMENT_RECORDED", request.referenceNumber());
        OrderPaymentEntity payment = new OrderPaymentEntity();
        payment.setOrder(order);
        payment.setFinanceUser(financeUser);
        payment.setReferenceNumber(request.referenceNumber());
        payment.setAmount(request.amount());
        payment.setRecordedAt(OffsetDateTime.now());
        orderPaymentRepository.save(payment);
        order.setPaymentRecorded(true);
        order.setPaymentRecordedAt(payment.getRecordedAt());
        auditService.record("ORDER_PAYMENT_RECORDED", financeUser.getUsername(), order.getOrderNumber());
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse pickPack(Long orderId) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_FULFILL);
        transition(order, OrderStatus.PICK_PACK, "PICK_PACK_STARTED", "Pick and pack started");
        order.setPickPackStartedAt(OffsetDateTime.now());
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse createShipment(Long orderId, ShipmentCreateRequest request) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_FULFILL);
        requireStatusIn(order, OrderStatus.PICK_PACK, OrderStatus.PARTIALLY_SHIPPED);
        UserEntity actor = currentUserService.requireCurrentUser();
        Map<Long, ProcurementOrderItemEntity> items = order.getItems().stream().collect(java.util.stream.Collectors.toMap(ProcurementOrderItemEntity::getId, item -> item));
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setOrder(order);
        shipment.setShipmentNumber(generateNumber("SHP"));
        shipment.setFulfillmentUser(actor);
        shipment.setStatus("SHIPPED");
        shipment.setShippedAt(OffsetDateTime.now());
        shipment.setNotes(request.notes());

        int shippedNow = 0;
        for (QuantityRequest quantityRequest : request.items()) {
            ProcurementOrderItemEntity item = requireItem(items, quantityRequest.orderItemId());
            int remaining = orderQuantityService.remainingToShip(item.getOrderedQuantity(), item.getShippedQuantity());
            if (quantityRequest.quantity() > remaining) {
                throw new ApiException(400, "Cannot ship more than approved quantity", List.of("orderItemId=" + item.getId()));
            }
            item.setShippedQuantity(item.getShippedQuantity() + quantityRequest.quantity());
            ShipmentItemEntity shipmentItem = new ShipmentItemEntity();
            shipmentItem.setShipment(shipment);
            shipmentItem.setOrderItem(item);
            shipmentItem.setQuantity(quantityRequest.quantity());
            shipment.getItems().add(shipmentItem);
            shippedNow += quantityRequest.quantity();
        }
        if (shippedNow == 0) {
            throw new ApiException(400, "Shipment must include quantities", List.of("No shipment quantities provided"));
        }
        shipmentRepository.save(shipment);
        order.setLastShippedAt(shipment.getShippedAt());
        order.setUpdatedAt(OffsetDateTime.now());
        boolean hasRemainingToShip = order.getItems().stream().anyMatch(item -> orderQuantityService.remainingToShip(item.getOrderedQuantity(), item.getShippedQuantity()) > 0);
        if (hasRemainingToShip) {
            transition(order, OrderStatus.PARTIALLY_SHIPPED, "SHIPMENT_CREATED", shipment.getShipmentNumber());
            auditService.record("ORDER_PARTIALLY_SHIPPED", actor.getUsername(), order.getOrderNumber());
        } else {
            transition(order, OrderStatus.SHIPPED, "SHIPMENT_CREATED", shipment.getShipmentNumber());
            auditService.record("ORDER_SHIPPED", actor.getUsername(), order.getOrderNumber());
        }
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse createReceipt(Long orderId, ReceiptCreateRequest request) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_RECEIVE);
        requireStatusIn(order, OrderStatus.PARTIALLY_SHIPPED, OrderStatus.SHIPPED);
        UserEntity buyer = currentUserService.requireCurrentUser();
        Map<Long, ProcurementOrderItemEntity> items = order.getItems().stream().collect(java.util.stream.Collectors.toMap(ProcurementOrderItemEntity::getId, item -> item));
        ReceiptEntity receipt = new ReceiptEntity();
        receipt.setOrder(order);
        receipt.setReceiptNumber(generateNumber("RCT"));
        receipt.setBuyer(buyer);
        receipt.setReceivedAt(OffsetDateTime.now());
        receipt.setNotes(request.notes());
        boolean discrepancy = false;

        for (var quantityRequest : request.items()) {
            ProcurementOrderItemEntity item = requireItem(items, quantityRequest.orderItemId());
            int remainingShipped = orderQuantityService.remainingToReceive(item.getShippedQuantity(), item.getReceivedQuantity());
            if (quantityRequest.quantity() > remainingShipped) {
                discrepancy = true;
                if (!request.discrepancyConfirmed()) {
                    throw new ApiException(400, "Receipt discrepancy confirmation required", List.of("orderItemId=" + item.getId()));
                }
            }
            int maxReceivable = item.getOrderedQuantity() - item.getReceivedQuantity();
            if (quantityRequest.quantity() > maxReceivable) {
                throw new ApiException(400, "Cannot receive more than ordered quantity", List.of("orderItemId=" + item.getId()));
            }
            item.setReceivedQuantity(item.getReceivedQuantity() + quantityRequest.quantity());
            if (quantityRequest.quantity() != remainingShipped || quantityRequest.quantity() != item.getOrderedQuantity()) {
                item.setDiscrepancyFlag(discrepancy || quantityRequest.discrepancyReason() != null);
            }
            ReceiptItemEntity receiptItem = new ReceiptItemEntity();
            receiptItem.setReceipt(receipt);
            receiptItem.setOrderItem(item);
            receiptItem.setQuantity(quantityRequest.quantity());
            receiptItem.setDiscrepancyReason(quantityRequest.discrepancyReason());
            receipt.getItems().add(receiptItem);
        }
        receipt.setHasDiscrepancy(discrepancy || request.items().stream().anyMatch(item -> item.discrepancyReason() != null && !item.discrepancyReason().isBlank()));
        receiptRepository.save(receipt);
        order.setLastReceivedAt(receipt.getReceivedAt());
        order.setUpdatedAt(OffsetDateTime.now());
        if (order.getItems().stream().allMatch(item -> item.getReceivedQuantity() >= item.getOrderedQuantity())) {
            transition(order, OrderStatus.RECEIVED, "ORDER_RECEIVED", receipt.getReceiptNumber());
        } else {
            appendHistory(order, buyer, order.getCurrentStatus(), order.getCurrentStatus(), "PARTIAL_RECEIPT_RECORDED", receipt.getReceiptNumber());
        }
        auditService.record("ORDER_RECEIVED", buyer.getUsername(), order.getOrderNumber());
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse createReturn(Long orderId, ReturnCreateRequest request) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_RETURN);
        if (order.getCurrentStatus() != OrderStatus.RECEIVED && order.getCurrentStatus() != OrderStatus.SHIPPED && order.getCurrentStatus() != OrderStatus.PARTIALLY_SHIPPED) {
            throw new ApiException(400, "Returns require a shipped or received order", List.of(order.getCurrentStatus().name()));
        }
        UserEntity buyer = currentUserService.requireCurrentUser();
        Map<Long, ProcurementOrderItemEntity> items = order.getItems().stream().collect(java.util.stream.Collectors.toMap(ProcurementOrderItemEntity::getId, item -> item));
        OrderReturnEntity orderReturn = new OrderReturnEntity();
        orderReturn.setOrder(order);
        orderReturn.setReturnNumber(generateNumber("RTN"));
        orderReturn.setBuyer(buyer);
        orderReturn.setReasonCode(request.reasonCode());
        orderReturn.setComments(request.comments());
        orderReturn.setCreatedAt(OffsetDateTime.now());
        for (QuantityRequest quantityRequest : request.items()) {
            ProcurementOrderItemEntity item = requireItem(items, quantityRequest.orderItemId());
            int returnable = item.getReceivedQuantity() - item.getReturnedQuantity();
            if (quantityRequest.quantity() > returnable) {
                throw new ApiException(400, "Cannot return more than received quantity", List.of("orderItemId=" + item.getId()));
            }
            item.setReturnedQuantity(item.getReturnedQuantity() + quantityRequest.quantity());
            ReturnItemEntity returnItem = new ReturnItemEntity();
            returnItem.setOrderReturn(orderReturn);
            returnItem.setOrderItem(item);
            returnItem.setQuantity(quantityRequest.quantity());
            orderReturn.getItems().add(returnItem);
        }
        orderReturnRepository.save(orderReturn);
        order.setReturnedAt(orderReturn.getCreatedAt());
        transition(order, OrderStatus.RETURNED, "RETURN_CREATED", orderReturn.getReturnNumber());
        return toDetail(order);
    }

    @Transactional
    public OrderDetailResponse createAfterSalesCase(Long orderId, AfterSalesCaseCreateRequest request) {
        ProcurementOrderEntity order = getManagedOrder(orderId, Permission.ORDER_EXCEPTION_CREATE);
        UserEntity buyer = currentUserService.requireCurrentUser();
        AfterSalesCaseEntity entity = new AfterSalesCaseEntity();
        entity.setOrder(order);
        entity.setCaseNumber(generateNumber("ASC"));
        entity.setBuyer(buyer);
        entity.setReasonCode(request.reasonCode());
        entity.setStructuredDetail(request.structuredDetail());
        entity.setCreatedAt(OffsetDateTime.now());
        if (request.orderItemId() != null) {
            ProcurementOrderItemEntity item = order.getItems().stream().filter(candidate -> candidate.getId().equals(request.orderItemId())).findFirst()
                .orElseThrow(() -> new ApiException(404, "Order item not found", List.of("orderItemId=" + request.orderItemId())));
            entity.setOrderItem(item);
        }
        afterSalesCaseRepository.save(entity);
        appendHistory(order, buyer, order.getCurrentStatus(), order.getCurrentStatus(), "AFTER_SALES_CASE_CREATED", entity.getCaseNumber());
        return toDetail(order);
    }

    private ProcurementOrderEntity getManagedOrder(Long orderId, Permission permission) {
        ProcurementOrderEntity order = orderRepository.findWithItemsById(orderId)
            .orElseThrow(() -> new ApiException(404, "Order not found", List.of("orderId=" + orderId)));
        UserEntity actor = currentUserService.requireCurrentUser();
        boolean allowed = permissionAuthorizationService.canAccessResource(
            actor,
            permission,
            order.getBuyer().getId(),
            order.getBuyer().getRole().getName(),
            order.getBuyer().getOrganizationCode()
        );
        if (!allowed) {
            throw new ApiException(403, "Access denied", List.of("ORDER_SCOPE_RESTRICTION", "permission=" + permission.name()));
        }
        return order;
    }

    private void requireStatus(ProcurementOrderEntity order, OrderStatus expected) {
        if (order.getCurrentStatus() != expected) {
            throw new ApiException(400, "Order is not in the expected status", List.of("expected=" + expected.name(), "actual=" + order.getCurrentStatus().name()));
        }
    }

    private void requireStatusIn(ProcurementOrderEntity order, OrderStatus... expectedStatuses) {
        for (OrderStatus expectedStatus : expectedStatuses) {
            if (order.getCurrentStatus() == expectedStatus) {
                return;
            }
        }
        String expected = java.util.Arrays.stream(expectedStatuses).map(OrderStatus::name).collect(java.util.stream.Collectors.joining(","));
        throw new ApiException(400, "Order is not in the expected status", List.of("expected=" + expected, "actual=" + order.getCurrentStatus().name()));
    }

    private void transition(ProcurementOrderEntity order, OrderStatus target, String eventType, String detail) {
        if (order.getCurrentStatus() == OrderStatus.APPROVED && target == OrderStatus.CANCELED) {
            throw new ApiException(400, "Approved orders cannot be canceled directly", List.of("Dual approval required in later phase"));
        }
        if (order.getCurrentStatus() != target) {
            stateMachineService.validateTransition(order.getCurrentStatus(), target);
            OrderStatus from = order.getCurrentStatus();
            order.setCurrentStatus(target);
            order.setUpdatedAt(OffsetDateTime.now());
            appendHistory(order, currentUserService.requireCurrentUser(), from, target, eventType, detail);
        } else {
            appendHistory(order, currentUserService.requireCurrentUser(), target, target, eventType, detail);
        }
    }

    private void appendHistory(ProcurementOrderEntity order, UserEntity actor, OrderStatus from, OrderStatus to, String eventType, String detail) {
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

    private ProcurementOrderItemEntity requireItem(Map<Long, ProcurementOrderItemEntity> items, Long orderItemId) {
        ProcurementOrderItemEntity item = items.get(orderItemId);
        if (item == null) {
            throw new ApiException(404, "Order item not found", List.of("orderItemId=" + orderItemId));
        }
        return item;
    }

    private String generateNumber(String prefix) {
        return prefix + "-" + OffsetDateTime.now().format(NUMBER_SUFFIX) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private OrderSummaryResponse toSummary(ProcurementOrderEntity order) {
        int totalOrdered = order.getItems().stream().mapToInt(ProcurementOrderItemEntity::getOrderedQuantity).sum();
        int totalShipped = order.getItems().stream().mapToInt(ProcurementOrderItemEntity::getShippedQuantity).sum();
        int totalReceived = order.getItems().stream().mapToInt(ProcurementOrderItemEntity::getReceivedQuantity).sum();
        boolean discrepancyOpen = order.getItems().stream().anyMatch(ProcurementOrderItemEntity::isDiscrepancyFlag);
        return new OrderSummaryResponse(order.getId(), order.getOrderNumber(), order.getCurrentStatus().name(), order.getBuyer().getDisplayName(), order.getBuyer().getOrganizationCode(), order.getCreatedAt(), totalOrdered, totalShipped, totalReceived, discrepancyOpen);
    }

    private OrderDetailResponse toDetail(ProcurementOrderEntity order) {
        List<ShipmentEntity> shipments = shipmentRepository.findByOrderIdOrderByShippedAtAsc(order.getId());
        List<ReceiptEntity> receipts = receiptRepository.findByOrderIdOrderByReceivedAtAsc(order.getId());
        List<OrderReturnEntity> returns = orderReturnRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        List<AfterSalesCaseEntity> afterSalesCases = afterSalesCaseRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        List<OrderStatusHistoryEntity> history = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        OrderReviewEntity review = orderReviewRepository.findByOrderId(order.getId()).orElse(null);
        OrderPaymentEntity payment = orderPaymentRepository.findByOrderId(order.getId()).orElse(null);

        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .sorted(Comparator.comparing(ProcurementOrderItemEntity::getId))
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getSkuSnapshot(),
                item.getProductNameSnapshot(),
                item.getUnitSnapshot(),
                item.getUnitPriceSnapshot(),
                item.getOrderedQuantity(),
                item.getShippedQuantity(),
                item.getReceivedQuantity(),
            item.getReturnedQuantity(),
                orderQuantityService.remainingToShip(item.getOrderedQuantity(), item.getShippedQuantity()),
                orderQuantityService.remainingToReceive(item.getShippedQuantity(), item.getReceivedQuantity()),
                item.isDiscrepancyFlag()
            ))
            .toList();

        return new OrderDetailResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCurrentStatus().name(),
            order.getBuyer().getDisplayName(),
            order.getBuyer().getOrganizationCode(),
            order.getNotes(),
            order.isPaymentRecorded(),
            order.getCreatedAt(),
            itemResponses,
            review == null ? null : new ReviewResponse(review.getDecision().name(), review.getReviewer().getDisplayName(), review.getComments(), review.getReviewedAt()),
            payment == null ? null : new PaymentResponse(payment.getReferenceNumber(), payment.getAmount(), payment.getFinanceUser().getDisplayName(), payment.getRecordedAt()),
            shipments.stream().map(shipment -> new ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getFulfillmentUser().getDisplayName(),
                shipment.getShippedAt(),
                shipment.getNotes(),
                shipment.getItems().stream().map(item -> new ShipmentItemResponse(item.getOrderItem().getId(), item.getOrderItem().getSkuSnapshot(), item.getQuantity())).toList()
            )).toList(),
            receipts.stream().map(receipt -> new ReceiptResponse(
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getBuyer().getDisplayName(),
                receipt.getReceivedAt(),
                receipt.isHasDiscrepancy(),
                receipt.getNotes(),
                receipt.getItems().stream().map(item -> new ReceiptItemResponse(item.getOrderItem().getId(), item.getOrderItem().getSkuSnapshot(), item.getQuantity(), item.getDiscrepancyReason())).toList()
            )).toList(),
            returns.stream().map(orderReturn -> new ReturnResponse(
                orderReturn.getId(),
                orderReturn.getReturnNumber(),
                orderReturn.getBuyer().getDisplayName(),
                orderReturn.getReasonCode(),
                orderReturn.getComments(),
                orderReturn.getCreatedAt(),
                orderReturn.getItems().stream().map(item -> new ReturnItemResponse(item.getOrderItem().getId(), item.getOrderItem().getSkuSnapshot(), item.getQuantity())).toList()
            )).toList(),
            afterSalesCases.stream().map(item -> new AfterSalesCaseResponse(item.getId(), item.getCaseNumber(), item.getOrderItem() == null ? null : item.getOrderItem().getId(), item.getReasonCode(), item.getStructuredDetail(), item.getCreatedAt())).toList(),
            history.stream().map(item -> new TraceabilityEventResponse(item.getEventType(), item.getFromStatus(), item.getToStatus(), item.getActorUser().getDisplayName(), item.getDetail(), item.getCreatedAt())).toList()
        );
    }
}
