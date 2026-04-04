package com.pharmaprocure.portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmaprocure.portal.dto.CheckInDtos.CheckInDetailResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CheckInSummaryResponse;
import com.pharmaprocure.portal.dto.CheckInDtos.CreateCheckInRequest;
import com.pharmaprocure.portal.dto.CheckInDtos.UpdateCheckInRequest;
import com.pharmaprocure.portal.exception.ApiException;
import com.pharmaprocure.portal.service.CheckInService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService checkInService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public CheckInController(CheckInService checkInService, ObjectMapper objectMapper, Validator validator) {
        this.checkInService = checkInService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CHECKIN_VIEW')")
    public ResponseEntity<Page<CheckInSummaryResponse>> list(
            @PageableDefault(size = 20, sort = "updatedAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(checkInService.list(pageable));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CHECKIN_CREATE')")
    public ResponseEntity<CheckInDetailResponse> create(@RequestPart("payload") String payload, @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(checkInService.create(parse(payload, CreateCheckInRequest.class), files));
    }

    @PutMapping(value = "/{checkInId}", consumes = {"multipart/form-data"})
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CHECKIN_EDIT')")
    public ResponseEntity<CheckInDetailResponse> update(@PathVariable Long checkInId, @RequestPart("payload") String payload, @RequestPart(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(checkInService.update(checkInId, parse(payload, UpdateCheckInRequest.class), files));
    }

    @GetMapping("/{checkInId}")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CHECKIN_VIEW')")
    public ResponseEntity<CheckInDetailResponse> get(@PathVariable Long checkInId) {
        return ResponseEntity.ok(checkInService.get(checkInId));
    }

    @GetMapping("/{checkInId}/attachments/{attachmentId}/download")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CHECKIN_DOWNLOAD')")
    public ResponseEntity<Resource> download(@PathVariable Long checkInId, @PathVariable Long attachmentId) {
        return checkInService.downloadAttachment(checkInId, attachmentId);
    }

    private <T> T parse(String payload, Class<T> type) {
        try {
            T parsed = objectMapper.readValue(payload, type);
            validate(parsed);
            return parsed;
        } catch (IOException ex) {
            throw new ApiException(400, "Invalid request payload", List.of("JSON_PARSE_ERROR"));
        }
    }

    private <T> void validate(T payload) {
        Set<ConstraintViolation<T>> violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            throw new ApiException(400, "Validation failed", violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList());
        }
    }
}
