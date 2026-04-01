package com.pharmaprocure.portal.entity;

import com.pharmaprocure.portal.enums.CriticalActionAuditAction;
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
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "critical_action_audit_events")
@Getter
@Setter
public class CriticalActionAuditEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private CriticalActionRequestEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private UserEntity actorUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CriticalActionAuditAction action;

    @Column(length = 1000)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
