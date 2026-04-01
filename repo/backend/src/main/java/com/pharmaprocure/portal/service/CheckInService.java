package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.CheckInDtos.CheckInAttachmentResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CheckInAuditResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CheckInDetailResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CheckInRevisionResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CheckInSummaryResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CreateCheckInRequest;
import com.pharmaprocure.portal.dto.CheckInDtos.UpdateCheckInRequest;
import com.pharmaprocure.portal.entity.CheckInAttachmentEntity;
import com.pharmaprocure.portal.entity.CheckInAuditEventEntity;
import com.pharmaprocure.portal.entity.CheckInEntity;
import com.pharmaprocure.portal.entity.CheckInRevisionEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DataScope;
import com.pharmaprocure.portal.enums.CheckInAuditAction;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.repository.CheckInAttachmentRepository;
import com.pharmaprocure.portal.repository.CheckInAuditEventRepository;
import com.pharmaprocure.portal.repository.CheckInRepository;
import com.pharmaprocure.portal.repository.CheckInRevisionRepository;
import com.pharmaprocure.portal.security.Permission;
import com.pharmaprocure.portal.security.PermissionAuthorizationService;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Arrays;
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
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final CheckInRevisionRepository checkInRevisionRepository;
    private final CheckInAttachmentRepository checkInAttachmentRepository;
    private final CheckInAuditEventRepository checkInAuditEventRepository;
    private final CurrentUserService currentUserService;
    private final DocumentFileValidationService documentFileValidationService;
    private final DocumentStorageService documentStorageService;
    private final DocumentHashingService documentHashingService;
    private final DocumentSignatureService documentSignatureService;
    private final CheckInRevisionDiffService checkInRevisionDiffService;
    private final CheckInAuditService checkInAuditService;
    private final PermissionAuthorizationService permissionAuthorizationService;

    public CheckInService(
        CheckInRepository checkInRepository,
        CheckInRevisionRepository checkInRevisionRepository,
        CheckInAttachmentRepository checkInAttachmentRepository,
        CheckInAuditEventRepository checkInAuditEventRepository,
        CurrentUserService currentUserService,
        DocumentFileValidationService documentFileValidationService,
        DocumentStorageService documentStorageService,
        DocumentHashingService documentHashingService,
        DocumentSignatureService documentSignatureService,
        CheckInRevisionDiffService checkInRevisionDiffService,
        CheckInAuditService checkInAuditService,
        PermissionAuthorizationService permissionAuthorizationService
    ) {
        this.checkInRepository = checkInRepository;
        this.checkInRevisionRepository = checkInRevisionRepository;
        this.checkInAttachmentRepository = checkInAttachmentRepository;
        this.checkInAuditEventRepository = checkInAuditEventRepository;
        this.currentUserService = currentUserService;
        this.documentFileValidationService = documentFileValidationService;
        this.documentStorageService = documentStorageService;
        this.documentHashingService = documentHashingService;
        this.documentSignatureService = documentSignatureService;
        this.checkInRevisionDiffService = checkInRevisionDiffService;
        this.checkInAuditService = checkInAuditService;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public Page<CheckInSummaryResponse> list(Pageable pageable) {
        UserEntity actor = currentUserService.requireCurrentUser();
        DataScope scope = permissionAuthorizationService.requireDataScope(actor, Permission.CHECKIN_VIEW);
        Long ownerUserId = (scope == DataScope.SELF) ? actor.getId() : null;
        String orgCode = (scope == DataScope.ORGANIZATION) ? actor.getOrganizationCode() : null;
        com.pharmaprocure.portal.enums.RoleName roleName = (scope == DataScope.TEAM) ? actor.getRole().getName() : null;
        Page<CheckInEntity> checkIns = checkInRepository.findFiltered(ownerUserId, orgCode, roleName, pageable);
        return checkIns.map(this::toSummary);
    }

    @Transactional
    public CheckInDetailResponse create(CreateCheckInRequest request, MultipartFile[] files) {
        UserEntity actor = currentUserService.requireCurrentUser();
        CheckInEntity checkIn = new CheckInEntity();
        checkIn.setOwnerUser(actor);
        checkIn.setCommentText(request.commentText());
        checkIn.setDeviceTimestamp(request.deviceTimestamp());
        checkIn.setServerReceivedAt(OffsetDateTime.now());
        checkIn.setLatitude(request.latitude());
        checkIn.setLongitude(request.longitude());
        checkIn.setCurrentRevisionNumber(1);
        checkIn.setCreatedAt(OffsetDateTime.now());
        checkIn.setUpdatedAt(OffsetDateTime.now());
        checkIn = checkInRepository.save(checkIn);

        CheckInRevisionEntity revision = createRevision(checkIn, actor, request.commentText(), request.deviceTimestamp(), request.latitude(), request.longitude(), List.of("commentText", "deviceTimestamp", "latitude", "longitude"));
        saveAttachments(checkIn, revision, files);
        checkInAuditService.record(checkIn, revision, actor, CheckInAuditAction.CREATED, "Initial check-in created");
        return get(checkIn.getId());
    }

    @Transactional
    public CheckInDetailResponse update(Long checkInId, UpdateCheckInRequest request, MultipartFile[] files) {
        CheckInEntity checkIn = requireCheckIn(checkInId, Permission.CHECKIN_EDIT);
        UserEntity actor = currentUserService.requireCurrentUser();
        List<String> changedFields = checkInRevisionDiffService.changedFields(checkIn, request);
        if (changedFields.isEmpty() && (files == null || files.length == 0 || Arrays.stream(files).allMatch(MultipartFile::isEmpty))) {
            throw new ApiException(400, "No changes detected", List.of("NO_REVISION_CREATED"));
        }

        checkIn.setCommentText(request.commentText());
        checkIn.setDeviceTimestamp(request.deviceTimestamp());
        checkIn.setLatitude(request.latitude());
        checkIn.setLongitude(request.longitude());
        checkIn.setCurrentRevisionNumber(checkIn.getCurrentRevisionNumber() + 1);
        checkIn.setUpdatedAt(OffsetDateTime.now());
        CheckInRevisionEntity revision = createRevision(checkIn, actor, request.commentText(), request.deviceTimestamp(), request.latitude(), request.longitude(), changedFields);
        saveAttachments(checkIn, revision, files);
        checkInAuditService.record(checkIn, revision, actor, CheckInAuditAction.UPDATED, String.join(",", changedFields));
        return get(checkInId);
    }

    @Transactional(readOnly = true)
    public CheckInDetailResponse get(Long checkInId) {
        CheckInEntity checkIn = requireCheckIn(checkInId, Permission.CHECKIN_VIEW);
        List<CheckInRevisionEntity> revisions = checkInRevisionRepository.findByCheckInIdOrderByRevisionNumberDesc(checkInId);
        List<CheckInAttachmentEntity> attachments = checkInAttachmentRepository.findByCheckInIdOrderByCreatedAtDesc(checkInId);
        List<CheckInAuditEventEntity> auditEvents = checkInAuditEventRepository.findByCheckInIdOrderByCreatedAtDesc(checkInId);
        return new CheckInDetailResponse(
            checkIn.getId(),
            checkIn.getOwnerUser().getDisplayName(),
            checkIn.getCommentText(),
            checkIn.getDeviceTimestamp(),
            checkIn.getServerReceivedAt(),
            checkIn.getLatitude(),
            checkIn.getLongitude(),
            attachments.stream().map(this::toAttachment).toList(),
            revisions.stream().map(this::toRevision).toList(),
            auditEvents.stream().map(event -> new CheckInAuditResponse(event.getAction().name(), event.getActorUser().getDisplayName(), event.getDetail(), event.getCreatedAt())).toList(),
            checkIn.getCreatedAt(),
            checkIn.getUpdatedAt()
        );
    }

    @Transactional
    public ResponseEntity<Resource> downloadAttachment(Long checkInId, Long attachmentId) {
        CheckInEntity checkIn = requireCheckIn(checkInId, Permission.CHECKIN_DOWNLOAD);
        CheckInAttachmentEntity attachment = checkInAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ApiException(404, "Attachment not found", List.of("attachmentId=" + attachmentId)));
        if (!attachment.getCheckIn().getId().equals(checkInId)) {
            throw new ApiException(404, "Attachment not found for check-in", List.of("attachmentId=" + attachmentId));
        }
        checkInAuditService.record(checkIn, attachment.getRevision(), currentUserService.requireCurrentUser(), CheckInAuditAction.DOWNLOADED, attachment.getOriginalFileName());
        Path path = documentStorageService.resolve(attachment.getStoragePath());
        FileSystemResource resource = new FileSystemResource(path);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getOriginalFileName() + "\"");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(attachment.getMimeType())).body(resource);
    }

    private CheckInEntity requireCheckIn(Long checkInId, Permission permission) {
        CheckInEntity checkIn = checkInRepository.findWithOwnerUserById(checkInId)
            .orElseThrow(() -> new ApiException(404, "Check-in not found", List.of("checkInId=" + checkInId)));
        UserEntity actor = currentUserService.requireCurrentUser();
        boolean allowed = permissionAuthorizationService.canAccessResource(
            actor,
            permission,
            checkIn.getOwnerUser().getId(),
            checkIn.getOwnerUser().getRole().getName(),
            checkIn.getOwnerUser().getOrganizationCode()
        );
        if (!allowed) {
            throw new ApiException(403, "Access denied", List.of("CHECKIN_SCOPE_RESTRICTION", "permission=" + permission.name()));
        }
        return checkIn;
    }

    private CheckInRevisionEntity createRevision(CheckInEntity checkIn, UserEntity actor, String commentText, OffsetDateTime deviceTimestamp, java.math.BigDecimal latitude, java.math.BigDecimal longitude, List<String> changedFields) {
        CheckInRevisionEntity revision = new CheckInRevisionEntity();
        revision.setCheckIn(checkIn);
        revision.setRevisionNumber(checkIn.getCurrentRevisionNumber());
        revision.setCommentText(commentText);
        revision.setDeviceTimestamp(deviceTimestamp);
        revision.setLatitude(latitude);
        revision.setLongitude(longitude);
        revision.setChangedFields(String.join(",", changedFields));
        revision.setEditedBy(actor);
        revision.setCreatedAt(OffsetDateTime.now());
        return checkInRevisionRepository.save(revision);
    }

    private void saveAttachments(CheckInEntity checkIn, CheckInRevisionEntity revision, MultipartFile[] files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            documentFileValidationService.validate(file);
            DocumentStorageService.StoredFileMetadata stored = documentStorageService.store("CHECKIN", "CHK-" + checkIn.getId(), revision.getRevisionNumber(), file);
            CheckInAttachmentEntity attachment = new CheckInAttachmentEntity();
            attachment.setCheckIn(checkIn);
            attachment.setRevision(revision);
            attachment.setOriginalFileName(stored.originalFileName());
            attachment.setStoredFileName(stored.storedFileName());
            attachment.setStoragePath(stored.storagePath());
            attachment.setMimeType(stored.mimeType());
            attachment.setFileSizeBytes(stored.fileSizeBytes());
            attachment.setSha256Hash(documentHashingService.sha256(stored.absolutePath()));
            attachment.setSignatureValue(documentSignatureService.sign(attachment.getSha256Hash()));
            attachment.setSignatureAlgorithm(documentSignatureService.algorithm());
            attachment.setSignerKeyId(documentSignatureService.keyId());
            attachment.setCreatedAt(OffsetDateTime.now());
            checkInAttachmentRepository.save(attachment);
        }
    }

    private CheckInSummaryResponse toSummary(CheckInEntity checkIn) {
        int revisionCount = checkInRevisionRepository.findByCheckInIdOrderByRevisionNumberDesc(checkIn.getId()).size();
        int attachmentCount = checkInAttachmentRepository.findByCheckInIdOrderByCreatedAtDesc(checkIn.getId()).size();
        return new CheckInSummaryResponse(checkIn.getId(), checkIn.getOwnerUser().getDisplayName(), checkIn.getCommentText(), checkIn.getDeviceTimestamp(), checkIn.getServerReceivedAt(), checkIn.getUpdatedAt(), revisionCount, checkIn.getLatitude() != null && checkIn.getLongitude() != null, attachmentCount);
    }

    private CheckInAttachmentResponse toAttachment(CheckInAttachmentEntity attachment) {
        return new CheckInAttachmentResponse(
            attachment.getId(),
            attachment.getOriginalFileName(),
            attachment.getMimeType(),
            attachment.getFileSizeBytes(),
            attachment.getSha256Hash(),
            attachment.getSignatureAlgorithm(),
            attachment.getSignerKeyId(),
            "/api/check-ins/" + attachment.getCheckIn().getId() + "/attachments/" + attachment.getId() + "/download",
            attachment.getCreatedAt()
        );
    }

    private CheckInRevisionResponse toRevision(CheckInRevisionEntity revision) {
        List<String> fields = revision.getChangedFields() == null || revision.getChangedFields().isBlank()
            ? List.of()
            : Arrays.stream(revision.getChangedFields().split(",")).filter(item -> !item.isBlank()).toList();
        return new CheckInRevisionResponse(revision.getId(), revision.getRevisionNumber(), revision.getCommentText(), revision.getDeviceTimestamp(), revision.getLatitude(), revision.getLongitude(), fields, revision.getEditedBy().getDisplayName(), revision.getCreatedAt());
    }
}
