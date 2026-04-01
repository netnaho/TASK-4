package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.CriticalActionDtos.CreateCriticalActionRequest;
import com.pharmaprocure.portal.dto.CriticalActionDtos.CriticalActionDecisionRequest;
import com.pharmaprocure.portal.dto.CriticalActionDtos.CriticalActionRequestResponse;
import com.pharmaprocure.portal.enums.CriticalActionDecision;
import com.pharmaprocure.portal.enums.CriticalActionStatus;
import com.pharmaprocure.portal.security.UserPrincipal;
import com.pharmaprocure.portal.service.CriticalActionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/critical-actions")
public class CriticalActionController {

    private final CriticalActionService criticalActionService;

    public CriticalActionController(CriticalActionService criticalActionService) {
        this.criticalActionService = criticalActionService;
    }

    @GetMapping
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CRITICAL_ACTION_VIEW')")
    public ResponseEntity<Page<CriticalActionRequestResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) CriticalActionStatus status) {
        return ResponseEntity.ok(criticalActionService.list(pageable, status));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CRITICAL_ACTION_VIEW')")
    public ResponseEntity<CriticalActionRequestResponse> get(@PathVariable Long requestId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(criticalActionService.get(requestId, principal));
    }

    @PostMapping
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CRITICAL_ACTION_REQUEST')")
    public ResponseEntity<CriticalActionRequestResponse> create(@Valid @RequestBody CreateCriticalActionRequest request) {
        return ResponseEntity.ok(criticalActionService.create(request));
    }

    @PostMapping("/{requestId}/decision")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'CRITICAL_ACTION_APPROVE')")
    public ResponseEntity<CriticalActionRequestResponse> decide(@PathVariable Long requestId, @Valid @RequestBody CriticalActionDecisionRequest request) {
        return ResponseEntity.ok(criticalActionService.decide(requestId, CriticalActionDecision.valueOf(request.decision().toUpperCase()), request.comments()));
    }
}
