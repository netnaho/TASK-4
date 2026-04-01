package com.pharmaprocure.portal.entity;

import com.pharmaprocure.portal.enums.CheckInAuditAction;
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
@Table(name = "check_in_audit_events")
@Getter
@Setter
public class CheckInAuditEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckInEntity checkIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id")
    private CheckInRevisionEntity revision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private UserEntity actorUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CheckInAuditAction action;

    @Column(length = 1000)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
