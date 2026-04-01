package com.pharmaprocure.portal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class OrderLifecycleDtos {

    private OrderLifecycleDtos() {
    }

    public record CreateOrderRequest(
        @NotEmpty(message = "Order items are required")
        List<@Valid CreateOrderItemRequest> items,
        String notes
    ) {
    }

    public record CreateOrderItemRequest(
        @NotNull(message = "Product id is required") Long productId,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity
    ) {
    }

    public record ReviewOrderRequest(
        @NotBlank(message = "Decision is required") String decision,
        String comments
    ) {
    }

    public record RecordPaymentRequest(
        @NotBlank(message = "Reference number is required") String referenceNumber,
        @NotNull(message = "Amount is required") BigDecimal amount
    ) {
    }

    public record ShipmentCreateRequest(
        @NotEmpty(message = "Shipment items are required")
        List<@Valid QuantityRequest> items,
        String notes
    ) {
    }

    public record ReceiptCreateRequest(
        @NotEmpty(message = "Receipt items are required")
        List<@Valid ReceiptQuantityRequest> items,
        String notes,
        boolean discrepancyConfirmed
    ) {
    }

    public record ReceiptQuantityRequest(
        @NotNull(message = "Order item id is required") Long orderItemId,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity,
        String discrepancyReason
    ) {
    }

    public record ReturnCreateRequest(
        @NotBlank(message = "Reason code is required") String reasonCode,
        @NotEmpty(message = "Return items are required")
        List<@Valid QuantityRequest> items,
        String comments
    ) {
    }

    public record AfterSalesCaseCreateRequest(
        Long orderItemId,
        @NotBlank(message = "Reason code is required") String reasonCode,
        @NotBlank(message = "Structured detail is required") String structuredDetail
    ) {
    }

    public record QuantityRequest(
        @NotNull(message = "Order item id is required") Long orderItemId,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity
    ) {
    }

    public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        String status,
        String buyer,
        String organizationCode,
        OffsetDateTime createdAt,
        int totalOrderedQuantity,
        int totalShippedQuantity,
        int totalReceivedQuantity,
        boolean discrepancyOpen
    ) {
    }

    public record OrderDetailResponse(
        Long id,
        String orderNumber,
        String status,
        String buyer,
        String organizationCode,
        String notes,
        boolean paymentRecorded,
        OffsetDateTime createdAt,
        List<OrderItemResponse> items,
        ReviewResponse review,
        PaymentResponse payment,
        List<ShipmentResponse> shipments,
        List<ReceiptResponse> receipts,
        List<ReturnResponse> returns,
        List<AfterSalesCaseResponse> afterSalesCases,
        List<TraceabilityEventResponse> timeline
    ) {
    }

    public record OrderItemResponse(
        Long id,
        Long productId,
        String sku,
        String name,
        String unit,
        BigDecimal unitPrice,
        int orderedQuantity,
        int shippedQuantity,
        int receivedQuantity,
        int returnedQuantity,
        int remainingToShip,
        int remainingToReceive,
        boolean discrepancyFlag
    ) {
    }

    public record ReviewResponse(String decision, String reviewer, String comments, OffsetDateTime reviewedAt) {
    }

    public record PaymentResponse(String referenceNumber, BigDecimal amount, String financeUser, OffsetDateTime recordedAt) {
    }

    public record ShipmentResponse(Long id, String shipmentNumber, String actor, OffsetDateTime shippedAt, String notes, List<ShipmentItemResponse> items) {
    }

    public record ShipmentItemResponse(Long orderItemId, String sku, int quantity) {
    }

    public record ReceiptResponse(Long id, String receiptNumber, String actor, OffsetDateTime receivedAt, boolean hasDiscrepancy, String notes, List<ReceiptItemResponse> items) {
    }

    public record ReceiptItemResponse(Long orderItemId, String sku, int quantity, String discrepancyReason) {
    }

    public record ReturnResponse(Long id, String returnNumber, String actor, String reasonCode, String comments, OffsetDateTime createdAt, List<ReturnItemResponse> items) {
    }

    public record ReturnItemResponse(Long orderItemId, String sku, int quantity) {
    }

    public record AfterSalesCaseResponse(Long id, String caseNumber, Long orderItemId, String reasonCode, String structuredDetail, OffsetDateTime createdAt) {
    }

    public record TraceabilityEventResponse(String eventType, String fromStatus, String toStatus, String actor, String detail, OffsetDateTime createdAt) {
    }
}
