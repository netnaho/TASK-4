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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_archives")
@Getter
@Setter
public class DocumentArchiveRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private DocumentVersionEntity version;

    @Column(nullable = false, length = 64)
    private String archiveHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signatureValue;

    @Column(nullable = false, length = 80)
    private String signatureAlgorithm;

    @Column(nullable = false, length = 120)
    private String signerKeyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by", nullable = false)
    private UserEntity archivedBy;

    @Column(nullable = false)
    private OffsetDateTime archivedAt;
}
