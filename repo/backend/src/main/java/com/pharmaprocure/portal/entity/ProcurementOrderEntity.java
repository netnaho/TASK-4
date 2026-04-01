package com.pharmaprocure.portal.entity;

import com.pharmaprocure.portal.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "procurement_orders")
@Getter
@Setter
public class ProcurementOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserEntity buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus currentStatus;

    @Column(nullable = false)
    private boolean paymentRecorded;

    @Column(nullable = false)
    private boolean reviewRequired;

    @Column
    private OffsetDateTime reviewCompletedAt;

    @Column
    private OffsetDateTime approvedAt;

    @Column
    private OffsetDateTime paymentRecordedAt;

    @Column
    private OffsetDateTime pickPackStartedAt;

    @Column
    private OffsetDateTime lastShippedAt;

    @Column
    private OffsetDateTime lastReceivedAt;

    @Column
    private OffsetDateTime returnedAt;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcurementOrderItemEntity> items = new ArrayList<>();
}
