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
@Table(name = "check_in_attachments")
@Getter
@Setter
public class CheckInAttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckInEntity checkIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id", nullable = false)
    private CheckInRevisionEntity revision;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String sha256Hash;

    @Column(name = "signature_value", nullable = false, columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signature_algorithm", nullable = false, length = 80)
    private String signatureAlgorithm;

    @Column(name = "signer_key_id", nullable = false, length = 120)
    private String signerKeyId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
