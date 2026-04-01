package com.pharmaprocure.portal.entity;

import com.pharmaprocure.portal.enums.DocumentApprovalStatus;
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
@Table(name = "document_approval_steps")
@Getter
@Setter
public class DocumentApprovalStepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private DocumentApprovalRouteEntity route;

    @Column(nullable = false)
    private int stepOrder;

    @Column(nullable = false, length = 60)
    private String approverRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private UserEntity approverUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentApprovalStatus status;

    @Column(length = 1000)
    private String comments;

    @Column
    private OffsetDateTime actedAt;
}
