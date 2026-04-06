package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.AdminDtos.CreateReasonCodeRequest;
import com.pharmaprocure.portal.dto.AdminDtos.PermissionOverviewResponse;
import com.pharmaprocure.portal.dto.AdminDtos.ReasonCodeResponse;
import com.pharmaprocure.portal.dto.AdminDtos.ResetUserPasswordRequest;
import com.pharmaprocure.portal.dto.AdminDtos.StateMachineConfigResponse;
import com.pharmaprocure.portal.dto.AdminDtos.StateMachineTransitionResponse;
import com.pharmaprocure.portal.dto.AdminDtos.UpdateUserAccessRequest;
import com.pharmaprocure.portal.dto.AdminDtos.UpdateStateMachineTransitionRequest;
import com.pharmaprocure.portal.dto.AdminDtos.UpdateDocumentTypeRequest;
import com.pharmaprocure.portal.dto.AdminDtos.UpdateReasonCodeRequest;
import com.pharmaprocure.portal.dto.AdminDtos.UserVisibilityResponse;
import com.pharmaprocure.portal.dto.DocumentDtos.DocumentTypeResponse;
import com.pharmaprocure.portal.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("@permissionAuth.hasPermission(authentication, 'ADMIN_CONFIG_VIEW')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserVisibilityResponse>> users() {
        return ResponseEntity.ok(adminService.users());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserVisibilityResponse> updateUserAccess(@PathVariable Long id, @Valid @RequestBody UpdateUserAccessRequest request) {
        return ResponseEntity.ok(adminService.updateUserAccess(id, request));
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<UserVisibilityResponse> resetUserPassword(@PathVariable Long id, @Valid @RequestBody ResetUserPasswordRequest request) {
        return ResponseEntity.ok(adminService.resetUserPassword(id, request.newPassword()));
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionOverviewResponse>> permissions() {
        return ResponseEntity.ok(adminService.permissions());
    }

    @GetMapping("/state-machine")
    public ResponseEntity<StateMachineConfigResponse> stateMachine() {
        return ResponseEntity.ok(adminService.stateMachine());
    }

    @PutMapping("/state-machine/{id}")
    public ResponseEntity<StateMachineTransitionResponse> updateStateMachineTransition(@PathVariable Long id, @Valid @RequestBody UpdateStateMachineTransitionRequest request) {
        return ResponseEntity.ok(adminService.updateStateMachineTransition(id, request));
    }

    @GetMapping("/document-types")
    public ResponseEntity<List<DocumentTypeResponse>> documentTypes() {
        return ResponseEntity.ok(adminService.documentTypes());
    }

    @PutMapping("/document-types/{id}")
    public ResponseEntity<DocumentTypeResponse> updateDocumentType(@PathVariable Long id, @Valid @RequestBody UpdateDocumentTypeRequest request) {
        return ResponseEntity.ok(adminService.updateDocumentType(id, request));
    }

    @GetMapping("/reason-codes")
    public ResponseEntity<List<ReasonCodeResponse>> reasonCodes() {
        return ResponseEntity.ok(adminService.reasonCodes());
    }

    @PostMapping("/reason-codes")
    public ResponseEntity<ReasonCodeResponse> createReasonCode(@Valid @RequestBody CreateReasonCodeRequest request) {
        return ResponseEntity.ok(adminService.createReasonCode(request));
    }

    @PutMapping("/reason-codes/{id}")
    public ResponseEntity<ReasonCodeResponse> updateReasonCode(@PathVariable Long id, @Valid @RequestBody UpdateReasonCodeRequest request) {
        return ResponseEntity.ok(adminService.updateReasonCode(id, request));
    }
}
