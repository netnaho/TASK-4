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
@Table(name = "document_versions")
@Getter
@Setter
public class DocumentVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "title_snapshot", nullable = false, length = 220)
    private String titleSnapshot;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", length = 255)
    private String storedFileName;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "mime_type", length = 120)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "sha256_hash", length = 64)
    private String sha256Hash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
