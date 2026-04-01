package com.pharmaprocure.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "procurement_order_items")
@Getter
@Setter
public class ProcurementOrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private ProcurementOrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductCatalogEntity product;

    @Column(nullable = false, length = 180)
    private String productNameSnapshot;

    @Column(nullable = false, length = 80)
    private String skuSnapshot;

    @Column(nullable = false, length = 32)
    private String unitSnapshot;

    @Column(nullable = false, precision = 14, scale = 2)
    private java.math.BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private int orderedQuantity;

    @Column(nullable = false)
    private int shippedQuantity;

    @Column(nullable = false)
    private int receivedQuantity;

    @Column(nullable = false)
    private int returnedQuantity;

    @Column(nullable = false)
    private int damagedQuantity;

    @Column(nullable = false)
    private boolean discrepancyFlag;
}
