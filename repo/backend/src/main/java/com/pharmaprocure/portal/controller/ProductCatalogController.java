package com.pharmaprocure.portal.controller;

import com.pharmaprocure.portal.dto.ProductCatalogResponse;
import com.pharmaprocure.portal.service.ProductCatalogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    public ProductCatalogController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping("/products")
    @PreAuthorize("@permissionAuth.hasPermission(authentication, 'ORDER_VIEW') or @permissionAuth.hasPermission(authentication, 'ORDER_CREATE')")
    public ResponseEntity<List<ProductCatalogResponse>> products() {
        return ResponseEntity.ok(productCatalogService.listProducts());
    }
}
