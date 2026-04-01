package com.pharmaprocure.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "system_audit_events")
@Getter
@Setter
public class SystemAuditEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(name = "actor_username", nullable = false, length = 120)
    private String actorUsername;

    @Column(length = 1000)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
