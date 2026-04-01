package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.VersionResponse;
import com.pharmaprocure.portal.service.MetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> version() {
        return ResponseEntity.ok(metaService.getVersion());
    }
}
