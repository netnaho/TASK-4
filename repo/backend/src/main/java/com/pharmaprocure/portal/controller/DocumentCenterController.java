package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.DocumentDtos.ApproveDocumentRequest;
import com.pharmaprocure.portal.dto.DocumentDtos.CreateDocumentTemplateRequest;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentArchiveResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentDetailResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentPreviewResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentSummaryResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentTemplateResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse;
import com.pharmaprocure.portal.enums.DocumentStatus;
import com.pharmaprocure.portal.service.DocumentCenterService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentCenterController {

    private final DocumentCenterService documentCenterService;

    public DocumentCenterController(DocumentCenterService documentCenterService) {
        this.documentCenterService = documentCenterService;
    }

    @GetMapping("/types")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<List<DocumentTypeResponse>> types() {
        return ResponseEntity.ok(documentCenterService.listTypes());
    }

    @GetMapping("/templates")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<List<DocumentTemplateResponse>> templates() {
        return ResponseEntity.ok(documentCenterService.listTemplates());
    }

    @PostMapping("/templates")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_TEMPLATE_MANAGE')")
    public ResponseEntity<DocumentTemplateResponse> createTemplate(@Valid @RequestBody CreateDocumentTemplateRequest request) {
        return ResponseEntity.ok(documentCenterService.createTemplate(request));
    }

    @GetMapping
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<Page<DocumentSummaryResponse>> documents(
            @PageableDefault(size = 20, sort = "updatedAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Long documentTypeId) {
        return ResponseEntity.ok(documentCenterService.listDocuments(pageable, status, documentTypeId));
    }

    @GetMapping("/approval-queue")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_APPROVE')")
    public ResponseEntity<Page<DocumentSummaryResponse>> approvalQueue(
            @PageableDefault(size = 20, sort = "updatedAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(documentCenterService.approvalQueue(pageable));
    }

    @GetMapping("/archive")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<List<DocumentArchiveResponse>> archive() {
        return ResponseEntity.ok(documentCenterService.archiveList());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_CREATE')")
    public ResponseEntity<DocumentDetailResponse> createDraft(@RequestPart("payload") String payload, @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(documentCenterService.createDraft(payload, file));
    }

    @PutMapping(value = "/{documentId}", consumes = {"multipart/form-data"})
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_EDIT')")
    public ResponseEntity<DocumentDetailResponse> updateDraft(@PathVariable Long documentId, @RequestPart("payload") String payload, @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(documentCenterService.updateDraft(documentId, payload, file));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentCenterService.getDocument(documentId));
    }

    @PostMapping("/{documentId}/submit-approval")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_EDIT')")
    public ResponseEntity<DocumentDetailResponse> submitApproval(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentCenterService.submitForApproval(documentId));
    }

    @PostMapping("/{documentId}/approve")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_APPROVE')")
    public ResponseEntity<DocumentDetailResponse> approve(@PathVariable Long documentId, @RequestBody(required = false) ApproveDocumentRequest request) {
        String comments = request == null ? null : request.comments();
        return ResponseEntity.ok(documentCenterService.approve(documentId, comments));
    }

    @PostMapping("/{documentId}/archive")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_ARCHIVE')")
    public ResponseEntity<DocumentDetailResponse> archive(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentCenterService.archive(documentId));
    }

    @GetMapping("/{documentId}/preview")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<DocumentPreviewResponse> preview(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentCenterService.preview(documentId));
    }

    @GetMapping("/{documentId}/content")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_VIEW')")
    public ResponseEntity<Resource> content(@PathVariable Long documentId) {
        return documentCenterService.download(documentId, false);
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_DOWNLOAD')")
    public ResponseEntity<Resource> download(@PathVariable Long documentId) {
        return documentCenterService.download(documentId, true);
    }
}
