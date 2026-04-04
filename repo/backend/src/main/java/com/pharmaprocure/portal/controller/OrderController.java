package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.OrderLifecycleDtos.AfterSalesCaseCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.CreateOrderRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ManagedReasonCodeResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.OrderDetailResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.OrderSummaryResponse;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReceiptCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.RecordPaymentRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReturnCreateRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ReviewOrderRequest;
import com.pharmaprocure.portal.dto.OrderLifecycleDtos.ShipmentCreateRequest;
import com.pharmaprocure.portal.enums.OrderStatus;
import com.pharmaprocure.portal.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_VIEW')")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.listOrders(pageable, status));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_VIEW')")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/reason-codes")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_VIEW')")
    public ResponseEntity<List<ManagedReasonCodeResponse>> reasonCodes(@RequestParam String codeType) {
        return ResponseEntity.ok(orderService.listActiveReasonCodes(codeType));
    }

    @PostMapping
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_CREATE')")
    public ResponseEntity<OrderDetailResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping("/{orderId}/submit-review")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_CREATE')")
    public ResponseEntity<OrderDetailResponse> submitForReview(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.submitForReview(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_CREATE')")
    public ResponseEntity<OrderDetailResponse> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @PostMapping("/{orderId}/approve")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_APPROVE')")
    public ResponseEntity<OrderDetailResponse> approve(@PathVariable Long orderId, @Valid @RequestBody ReviewOrderRequest request) {
        return ResponseEntity.ok(orderService.review(orderId, request));
    }

    @PostMapping("/{orderId}/record-payment")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_PAYMENT_RECORD')")
    public ResponseEntity<OrderDetailResponse> recordPayment(@PathVariable Long orderId, @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.ok(orderService.recordPayment(orderId, request));
    }

    @PostMapping("/{orderId}/pick-pack")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_FULFILL')")
    public ResponseEntity<OrderDetailResponse> pickPack(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.pickPack(orderId));
    }

    @PostMapping("/{orderId}/shipments")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_FULFILL')")
    public ResponseEntity<OrderDetailResponse> createShipment(@PathVariable Long orderId, @Valid @RequestBody ShipmentCreateRequest request) {
        return ResponseEntity.ok(orderService.createShipment(orderId, request));
    }

    @PostMapping("/{orderId}/receipts")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_RECEIVE')")
    public ResponseEntity<OrderDetailResponse> createReceipt(@PathVariable Long orderId, @Valid @RequestBody ReceiptCreateRequest request) {
        return ResponseEntity.ok(orderService.createReceipt(orderId, request));
    }

    @PostMapping("/{orderId}/returns")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_RETURN')")
    public ResponseEntity<OrderDetailResponse> createReturn(@PathVariable Long orderId, @Valid @RequestBody ReturnCreateRequest request) {
        return ResponseEntity.ok(orderService.createReturn(orderId, request));
    }

    @PostMapping("/{orderId}/after-sales-cases")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_EXCEPTION_CREATE')")
    public ResponseEntity<OrderDetailResponse> createAfterSalesCase(@PathVariable Long orderId, @Valid @RequestBody AfterSalesCaseCreateRequest request) {
        return ResponseEntity.ok(orderService.createAfterSalesCase(orderId, request));
    }

    @GetMapping("/{orderId}/traceability")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_VIEW')")
    public ResponseEntity<OrderDetailResponse> traceability(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
}
