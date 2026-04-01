package com.pharmaprocure.portal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "shipments")
@Getter
@Setter
public class ShipmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private ProcurementOrderEntity order;

    @Column(nullable = false, unique = true, length = 40)
    private String shipmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_user_id", nullable = false)
    private UserEntity fulfillmentUser;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private OffsetDateTime shippedAt;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItemEntity> items = new ArrayList<>();
}
