package com.pharmaprocure.portal.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationProbeController {

    @GetMapping("/api/orders/workspace")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_VIEW')")
    public ResponseEntity<Map<String, String>> ordersWorkspace() {
        return ResponseEntity.ok(Map.of("resource", "orders", "access", "granted"));
    }

    @GetMapping("/api/documents/review")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'DOCUMENT_APPROVE')")
    public ResponseEntity<Map<String, String>> documentReview() {
        return ResponseEntity.ok(Map.of("resource", "documents", "access", "granted"));
    }

    @GetMapping("/api/admin/panel")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR') and @permissionAuth.hasPermission(authentication, 'ADMIN_ACCESS')")
    public ResponseEntity<Map<String, String>> adminPanel() {
        return ResponseEntity.ok(Map.of("resource", "admin", "access", "granted"));
    }
}
