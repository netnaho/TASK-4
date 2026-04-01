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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "check_in_revisions")
@Getter
@Setter
public class CheckInRevisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckInEntity checkIn;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Column(name = "comment_text", length = 4000)
    private String commentText;

    @Column(name = "device_timestamp", nullable = false)
    private OffsetDateTime deviceTimestamp;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "changed_fields", length = 500)
    private String changedFields;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private UserEntity editedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
