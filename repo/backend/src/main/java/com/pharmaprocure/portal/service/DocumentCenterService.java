package com.pharmaprocure.portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmaprocure.portal.dto.DocumentDtos.CreateDocumentDraftRequest;
import com.pharmaprocure.portal.dto.DocumentDtos.CreateDocumentTemplateRequest;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentApprovalStepResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentArchiveResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentAuditResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentDetailResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentPreviewResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentSummaryResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentTemplateResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentVersionResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.UpdateDocumentDraftRequest;
import com.pharmaprocure.portal.entity.DocumentApprovalRouteEntity;
import com.pharmaprocure.portal.entity.DocumentApprovalStepEntity;
import com.pharmaprocure.portal.entity.DocumentArchiveRecordEntity;
import com.pharmaprocure.portal.entity.DocumentAuditEventEntity;
import com.pharmaprocure.portal.entity.DocumentEntity;
import com.pharmaprocure.portal.entity.DocumentTemplateEntity;
import com.pharmaprocure.portal.entity.DocumentTypeEntity;
import com.pharmaprocure.portal.entity.DocumentVersionEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.DocumentApprovalStatus;
import com.pharmaprocure.portal.enums.DocumentAuditAction;
import com.pharmaprocure.portal.enums.DocumentStatus;
import com.pharmaprocure.portal.enums.RoleName;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.DocumentApprovalRouteRepository;
import com.pharmaprocure.portal.repository.DocumentArchiveRecordRepository;
import com.pharmaprocure.portal.repository.DocumentAuditEventRepository;
import com.pharmaprocure.portal.repository.DocumentRepository;
import com.pharmaprocure.portal.repository.DocumentTemplateRepository;
import com.pharmaprocure.portal.repository.DocumentTypeRepository;
import com.pharmaprocure.portal.repository.DocumentVersionRepository;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentCenterService {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentApprovalRouteRepository documentApprovalRouteRepository;
    private final DocumentArchiveRecordRepository documentArchiveRecordRepository;
    private final DocumentAuditEventRepository documentAuditEventRepository;
    private final CurrentUserService currentUserService;
    private final DocumentNumberingService documentNumberingService;
    private final DocumentFileValidationService documentFileValidationService;
    private final DocumentStorageService documentStorageService;
    private final DocumentHashingService documentHashingService;
    private final DocumentSignatureService documentSignatureService;
    private final DocumentAuditService documentAuditService;
    private final DocumentApprovalWorkflowService documentApprovalWorkflowService;
    private final ObjectMapper objectMapper;
    private final PermissionAuthorizationService permissionAuthorizationService;

    public DocumentCenterService(
        DocumentTypeRepository documentTypeRepository,
        DocumentTemplateRepository documentTemplateRepository,
        DocumentRepository documentRepository,
        DocumentVersionRepository documentVersionRepository,
        DocumentApprovalRouteRepository documentApprovalRouteRepository,
        DocumentArchiveRecordRepository documentArchiveRecordRepository,
        DocumentAuditEventRepository documentAuditEventRepository,
        CurrentUserService currentUserService,
        DocumentNumberingService documentNumberingService,
        DocumentFileValidationService documentFileValidationService,
        DocumentStorageService documentStorageService,
        DocumentHashingService documentHashingService,
        DocumentSignatureService documentSignatureService,
        DocumentAuditService documentAuditService,
        DocumentApprovalWorkflowService documentApprovalWorkflowService,
        ObjectMapper objectMapper,
        PermissionAuthorizationService permissionAuthorizationService
    ) {
        this.documentTypeRepository = documentTypeRepository;
        this.documentTemplateRepository = documentTemplateRepository;
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.documentApprovalRouteRepository = documentApprovalRouteRepository;
        this.documentArchiveRecordRepository = documentArchiveRecordRepository;
        this.documentAuditEventRepository = documentAuditEventRepository;
        this.currentUserService = currentUserService;
        this.documentNumberingService = documentNumberingService;
        this.documentFileValidationService = documentFileValidationService;
        this.documentStorageService = documentStorageService;
        this.documentHashingService = documentHashingService;
        this.documentSignatureService = documentSignatureService;
        this.documentAuditService = documentAuditService;
        this.documentApprovalWorkflowService = documentApprovalWorkflowService;
        this.objectMapper = objectMapper;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<DocumentTypeResponse> listTypes() {
        return documentTypeRepository.findAll().stream()
            .sorted(Comparator.comparing(DocumentTypeEntity::getCode))
            .map(type -> new DocumentTypeResponse(type.getId(), type.getCode(), type.getName(), type.getDescription(), type.isEvidenceAllowed(), type.isActive()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentTemplateResponse> listTemplates() {
        return documentTemplateRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(template -> new DocumentTemplateResponse(template.getId(), template.getDocumentType().getId(), template.getDocumentType().getCode(), template.getTemplateName(), template.getTemplateBody(), template.isActive()))
            .toList();
    }

    @Transactional
    public DocumentTemplateResponse createTemplate(CreateDocumentTemplateRequest request) {
        UserEntity actor = currentUserService.requireCurrentUser();
        DocumentTypeEntity type = requireType(request.documentTypeId());
        DocumentTemplateEntity template = new DocumentTemplateEntity();
        template.setDocumentType(type);
        template.setTemplateName(request.templateName());
        template.setTemplateBody(request.templateBody());
        template.setActive(request.active());
        template.setCreatedBy(actor);
        template.setCreatedAt(OffsetDateTime.now());
        DocumentTemplateEntity saved = documentTemplateRepository.save(template);
        return new DocumentTemplateResponse(saved.getId(), type.getId(), type.getCode(), saved.getTemplateName(), saved.getTemplateBody(), saved.isActive());
    }

    @Transactional(readOnly = true)
    public Page<DocumentSummaryResponse> listDocuments(Pageable pageable, DocumentStatus statusFilter, Long documentTypeId) {
        UserEntity actor = currentUserService.requireCurrentUser();
        DataScope scope = permissionAuthorizationService.requireDataScope(actor, Permission.DOCUMENT_VIEW);
        Long ownerUserId = (scope == DataScope.SELF) ? actor.getId() : null;
        String orgCode = (scope == DataScope.ORGANIZATION) ? actor.getOrganizationCode() : null;
        RoleName roleName = (scope == DataScope.TEAM) ? actor.getRole().getName() : null;
        Page<DocumentEntity> documents = documentRepository.findFiltered(ownerUserId, orgCode, roleName, statusFilter, documentTypeId, pageable);
        return documents.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<DocumentSummaryResponse> approvalQueue(Pageable pageable) {
        UserEntity actor = currentUserService.requireCurrentUser();
        DataScope scope = permissionAuthorizationService.requireDataScope(actor, Permission.DOCUMENT_APPROVE);
        Long ownerUserId = (scope == DataScope.SELF) ? actor.getId() : null;
        String orgCode = (scope == DataScope.ORGANIZATION) ? actor.getOrganizationCode() : null;
        RoleName roleName = (scope == DataScope.TEAM) ? actor.getRole().getName() : null;
        Page<DocumentEntity> documents = documentRepository.findFiltered(ownerUserId, orgCode, roleName, DocumentStatus.IN_APPROVAL, null, pageable);
        return documents.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<DocumentArchiveResponse> archiveList() {
        UserEntity actor = currentUserService.requireCurrentUser();
        return documentArchiveRecordRepository.findAllByOrderByArchivedAtDesc().stream()
            .filter(archive -> permissionAuthorizationService.canAccessResource(actor, Permission.DOCUMENT_VIEW, archive.getDocument().getOwnerUser().getId(), archive.getDocument().getOwnerUser().getRole().getName(), archive.getDocument().getOwnerUser().getOrganizationCode()))
            .map(archive -> new DocumentArchiveResponse(archive.getId(), archive.getArchiveHash(), archive.getSignatureAlgorithm(), archive.getSignerKeyId(), archive.getArchivedAt(), archive.getArchivedBy().getDisplayName()))
            .toList();
    }

    @Transactional
    public DocumentDetailResponse createDraft(String requestJson, MultipartFile file) {
        CreateDocumentDraftRequest request = parse(requestJson, CreateDocumentDraftRequest.class);
        UserEntity actor = currentUserService.requireCurrentUser();
        DocumentTypeEntity type = requireType(request.documentTypeId());
        DocumentTemplateEntity template = request.templateId() == null ? null : documentTemplateRepository.findById(request.templateId())
            .orElseThrow(() -> new ApiException(404, "Document template not found", List.of("templateId=" + request.templateId())));

        DocumentEntity document = new DocumentEntity();
        document.setDocumentType(type);
        document.setTemplate(template);
        document.setOwnerUser(actor);
        document.setTitle(request.title());
        document.setStatus(DocumentStatus.DRAFT);
        document.setMetadataTags(request.metadataTags());
        document.setCreatedAt(OffsetDateTime.now());
        document.setUpdatedAt(OffsetDateTime.now());
        document = documentRepository.save(document);

        DocumentVersionEntity version = createVersion(document, actor, request.title(), request.contentText(), file, 1);
        document.setCurrentVersion(version);
        documentRepository.save(document);
        createApprovalRoute(document, version, request.approvalRoles());
        documentAuditService.record(document, version, actor, DocumentAuditAction.CREATED, request.title());
        return getDocument(document.getId());
    }

    @Transactional
    public DocumentDetailResponse updateDraft(Long documentId, String requestJson, MultipartFile file) {
        UpdateDocumentDraftRequest request = parse(requestJson, UpdateDocumentDraftRequest.class);
        DocumentEntity document = requireDocument(documentId, Permission.DOCUMENT_EDIT);
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new ApiException(400, "Only draft documents can be updated", List.of(document.getStatus().name()));
        }
        UserEntity actor = currentUserService.requireCurrentUser();
        int nextVersion = documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId).stream().mapToInt(DocumentVersionEntity::getVersionNumber).max().orElse(0) + 1;
        document.setTitle(request.title());
        document.setMetadataTags(request.metadataTags());
        document.setUpdatedAt(OffsetDateTime.now());
        DocumentVersionEntity version = createVersion(document, actor, request.title(), request.contentText(), file, nextVersion);
        document.setCurrentVersion(version);
        documentRepository.save(document);
        documentAuditService.record(document, version, actor, DocumentAuditAction.UPDATED, request.title());
        return getDocument(documentId);
    }

    @Transactional
    public DocumentDetailResponse submitForApproval(Long documentId) {
        DocumentEntity document = requireDocument(documentId, Permission.DOCUMENT_EDIT);
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new ApiException(400, "Only draft documents can be submitted", List.of(document.getStatus().name()));
        }
        document.setDocumentNumber(documentNumberingService.nextNumber(document.getDocumentType().getCode()));
        document.setStatus(DocumentStatus.IN_APPROVAL);
        document.setUpdatedAt(OffsetDateTime.now());
        DocumentApprovalRouteEntity route = documentApprovalRouteRepository.findByDocumentId(documentId)
            .orElseThrow(() -> new ApiException(400, "Approval route missing", List.of("documentId=" + documentId)));
        if (route.getSteps().isEmpty()) {
            throw new ApiException(400, "Approval route requires at least one approver role", List.of("documentId=" + documentId));
        }
        route.setStatus(DocumentApprovalStatus.PENDING);
        documentAuditService.record(document, document.getCurrentVersion(), currentUserService.requireCurrentUser(), DocumentAuditAction.SUBMITTED_FOR_APPROVAL, document.getDocumentNumber());
        return getDocument(documentId);
    }

    @Transactional
    public DocumentDetailResponse approve(Long documentId, String comments) {
        DocumentEntity document = requireDocument(documentId, Permission.DOCUMENT_APPROVE);
        DocumentApprovalRouteEntity route = documentApprovalRouteRepository.findByDocumentId(documentId)
            .orElseThrow(() -> new ApiException(400, "Approval route missing", List.of("documentId=" + documentId)));
        DocumentApprovalStepEntity step = documentApprovalWorkflowService.nextPendingStep(route.getSteps());
        if (step == null) {
            throw new ApiException(400, "No pending approval step", List.of("documentId=" + documentId));
        }
        UserEntity actor = currentUserService.requireCurrentUser();
        documentApprovalWorkflowService.approveStep(step, actor, comments);
        if (documentApprovalWorkflowService.allApproved(route.getSteps())) {
            route.setStatus(DocumentApprovalStatus.APPROVED);
            document.setStatus(DocumentStatus.APPROVED);
        }
        document.setUpdatedAt(OffsetDateTime.now());
        documentAuditService.record(document, document.getCurrentVersion(), actor, DocumentAuditAction.APPROVED, comments);
        return getDocument(documentId);
    }

    @Transactional
    public DocumentDetailResponse archive(Long documentId) {
        DocumentEntity document = requireDocument(documentId, Permission.DOCUMENT_ARCHIVE);
        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new ApiException(400, "Only approved documents can be archived", List.of(document.getStatus().name()));
        }
        DocumentVersionEntity version = document.getCurrentVersion();
        if (version.getStoragePath() == null) {
            throw new ApiException(400, "Archived documents require a stored file", List.of("NO_FILE_ATTACHED"));
        }
        Path path = documentStorageService.resolve(version.getStoragePath());
        String archiveHash = documentHashingService.sha256(path);
        DocumentArchiveRecordEntity archive = new DocumentArchiveRecordEntity();
        archive.setDocument(document);
        archive.setVersion(version);
        archive.setArchiveHash(archiveHash);
        archive.setSignatureValue(documentSignatureService.sign(archiveHash));
        archive.setSignatureAlgorithm(documentSignatureService.algorithm());
        archive.setSignerKeyId(documentSignatureService.keyId());
        archive.setArchivedBy(currentUserService.requireCurrentUser());
        archive.setArchivedAt(OffsetDateTime.now());
        documentArchiveRecordRepository.save(archive);
        document.setStatus(DocumentStatus.ARCHIVED);
        document.setUpdatedAt(OffsetDateTime.now());
        documentAuditService.record(document, version, currentUserService.requireCurrentUser(), DocumentAuditAction.ARCHIVED, archiveHash);
        return getDocument(documentId);
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse getDocument(Long documentId) {
        DocumentEntity document = requireDocument(documentId, Permission.DOCUMENT_VIEW);
        List<DocumentVersionEntity> versions = documentVersionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);
        DocumentApprovalRouteEntity route = documentApprovalRouteRepository.findByDocumentId(documentId).orElse(null);
        DocumentArchiveRecordEntity archive = documentArchiveRecordRepository.findAllByOrderByArchivedAtDesc().stream()
            .filter(item -> item.getDocument().getId().equals(documentId))
            .findFirst()
            .orElse(null);
        List<DocumentAuditEventEntity> auditEvents = documentAuditEventRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
        return new DocumentDetailResponse(
            document.getId(),
            document.getDocumentNumber(),
            document.getTitle(),
            document.getDocumentType().getCode(),
            document.getStatus().name(),
            document.getOwnerUser().getDisplayName(),
            document.getMetadataTags(),
            versions.stream().map(this::toVersion).toList(),
            route == null ? List.of() : route.getSteps().stream().sorted(Comparator.comparingInt(DocumentApprovalStepEntity::getStepOrder)).map(this::toStep).toList(),
            archive == null ? null : new DocumentArchiveResponse(archive.getId(), archive.getArchiveHash(), archive.getSignatureAlgorithm(), archive.getSignerKeyId(), archive.getArchivedAt(), archive.getArchivedBy().getDisplayName()),
            auditEvents.stream().map(item -> new DocumentAuditResponse(item.getAction().name(), item.getActorUser().getDisplayName(), item.getDetail(), item.getCreatedAt())).toList(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }

    @Transactional
    public DocumentPreviewResponse preview(Long documentId) {
        DocumentEntity document = requireDocument(documentId, Permission.DOCUMENT_VIEW);
        if (document.getStatus() == DocumentStatus.DESTROYED) {
            throw new ApiException(400, "Destroyed documents cannot be previewed", List.of("DOCUMENT_DESTROYED"));
        }
        DocumentVersionEntity version = document.getCurrentVersion();
        documentAuditService.record(document, version, currentUserService.requireCurrentUser(), DocumentAuditAction.PREVIEWED, version.getOriginalFileName());
        String username = currentUserService.requireCurrentUser().getUsername();
        return new DocumentPreviewResponse(documentId, version.getId(), document.getDocumentNumber(), version.getMimeType(), document.getTitle(), "/api/documents/" + documentId + "/content", username + " | " + OffsetDateTime.now() + " | " + document.getDocumentNumber(), supportsPreview(version.getMimeType()));
    }

    @Transactional
    public ResponseEntity<Resource> download(Long documentId, boolean download) {
        Permission permission = download ? Permission.DOCUMENT_DOWNLOAD : Permission.DOCUMENT_VIEW;
        DocumentEntity document = requireDocument(documentId, permission);
        if (document.getStatus() == DocumentStatus.DESTROYED) {
            throw new ApiException(400, "Destroyed documents cannot be downloaded", List.of("DOCUMENT_DESTROYED"));
        }
        DocumentVersionEntity version = document.getCurrentVersion();
        if (version.getStoragePath() == null) {
            throw new ApiException(404, "Document file not found", List.of("NO_FILE"));
        }
        Path path = documentStorageService.resolve(version.getStoragePath());
        documentAuditService.record(document, version, currentUserService.requireCurrentUser(), DocumentAuditAction.DOWNLOADED, version.getOriginalFileName());
        FileSystemResource resource = new FileSystemResource(path);
        HttpHeaders headers = new HttpHeaders();
        if (download) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + version.getOriginalFileName() + "\"");
        }
        return ResponseEntity.ok().headers(headers).contentType(version.getMimeType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(version.getMimeType())).body(resource);
    }

    private DocumentVersionEntity createVersion(DocumentEntity document, UserEntity actor, String title, String contentText, MultipartFile file, int versionNumber) {
        DocumentVersionEntity version = new DocumentVersionEntity();
        version.setDocument(document);
        version.setVersionNumber(versionNumber);
        version.setTitleSnapshot(title);
        version.setContentText(contentText);
        version.setCreatedBy(actor);
        version.setCreatedAt(OffsetDateTime.now());
        if (file != null && !file.isEmpty()) {
            documentFileValidationService.validate(file);
            String documentNumber = document.getDocumentNumber() == null ? "DRAFT-" + document.getId() : document.getDocumentNumber();
            DocumentStorageService.StoredFileMetadata stored = documentStorageService.store(document.getDocumentType().getCode(), documentNumber, versionNumber, file);
            version.setOriginalFileName(stored.originalFileName());
            version.setStoredFileName(stored.storedFileName());
            version.setStoragePath(stored.storagePath());
            version.setMimeType(stored.mimeType());
            version.setFileSizeBytes(stored.fileSizeBytes());
            version.setSha256Hash(documentHashingService.sha256(stored.absolutePath()));
        }
        return documentVersionRepository.save(version);
    }

    private void createApprovalRoute(DocumentEntity document, DocumentVersionEntity version, List<String> approvalRoles) {
        DocumentApprovalRouteEntity route = new DocumentApprovalRouteEntity();
        route.setDocument(document);
        route.setVersion(version);
        route.setStatus(DocumentApprovalStatus.PENDING);
        route.setCreatedAt(OffsetDateTime.now());
        route = documentApprovalRouteRepository.save(route);
        int order = 1;
        for (String approvalRole : approvalRoles) {
            DocumentApprovalStepEntity step = new DocumentApprovalStepEntity();
            step.setRoute(route);
            step.setStepOrder(order++);
            step.setApproverRole(normalizeApprovalRole(approvalRole));
            step.setStatus(DocumentApprovalStatus.PENDING);
            route.getSteps().add(step);
        }
        documentApprovalRouteRepository.save(route);
    }

    private String normalizeApprovalRole(String approvalRole) {
        if (approvalRole == null || approvalRole.isBlank()) {
            throw new ApiException(400, "Approval role is required", List.of("APPROVAL_ROLE_MISSING"));
        }
        try {
            return RoleName.valueOf(approvalRole.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new ApiException(400, "Invalid approval role", List.of("approvalRole=" + approvalRole));
        }
    }

    private DocumentEntity requireDocument(Long documentId, Permission permission) {
        DocumentEntity document = documentRepository.findWithCurrentVersionById(documentId)
            .orElseThrow(() -> new ApiException(404, "Document not found", List.of("documentId=" + documentId)));
        UserEntity actor = currentUserService.requireCurrentUser();
        boolean allowed = permissionAuthorizationService.canAccessResource(
            actor,
            permission,
            document.getOwnerUser().getId(),
            document.getOwnerUser().getRole().getName(),
            document.getOwnerUser().getOrganizationCode()
        );
        if (!allowed) {
            throw new ApiException(403, "Access denied", List.of("DOCUMENT_SCOPE_RESTRICTION", "permission=" + permission.name()));
        }
        return document;
    }

    private DocumentTypeEntity requireType(Long documentTypeId) {
        return documentTypeRepository.findById(documentTypeId)
            .orElseThrow(() -> new ApiException(404, "Document type not found", List.of("documentTypeId=" + documentTypeId)));
    }

    private DocumentSummaryResponse toSummary(DocumentEntity document) {
        return new DocumentSummaryResponse(document.getId(), document.getDocumentNumber(), document.getTitle(), document.getDocumentType().getCode(), document.getStatus().name(), document.getOwnerUser().getDisplayName(), document.getMetadataTags(), document.getUpdatedAt());
    }

    private DocumentVersionResponse toVersion(DocumentVersionEntity version) {
        return new DocumentVersionResponse(version.getId(), version.getVersionNumber(), version.getTitleSnapshot(), version.getContentText(), version.getOriginalFileName(), version.getMimeType(), version.getFileSizeBytes(), version.getSha256Hash(), version.getCreatedAt(), version.getCreatedBy().getDisplayName());
    }

    private DocumentApprovalStepResponse toStep(DocumentApprovalStepEntity step) {
        return new DocumentApprovalStepResponse(step.getId(), step.getStepOrder(), step.getApproverRole(), step.getApproverUser() == null ? null : step.getApproverUser().getDisplayName(), step.getStatus().name(), step.getComments(), step.getActedAt());
    }

    private boolean supportsPreview(String mimeType) {
        return "application/pdf".equals(mimeType) || "image/png".equals(mimeType) || "image/jpeg".equals(mimeType) || "audio/wav".equals(mimeType);
    }

    private <T> T parse(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (IOException ex) {
            throw new ApiException(400, "Invalid request payload", List.of("JSON_PARSE_ERROR"));
        }
    }
}
