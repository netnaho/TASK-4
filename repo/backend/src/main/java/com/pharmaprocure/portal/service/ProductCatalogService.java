package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.ProductCatalogResponse;
import com.pharmaprocure.portal.repository.ProductCatalogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogService {

    private final ProductCatalogRepository productCatalogRepository;

    public ProductCatalogService(ProductCatalogRepository productCatalogRepository) {
        this.productCatalogRepository = productCatalogRepository;
    }

    public List<ProductCatalogResponse> listProducts() {
        return productCatalogRepository.findAll().stream()
            .map(product -> new ProductCatalogResponse(product.getId(), product.getSku(), product.getName(), product.getUnitPrice(), product.getUnit()))
            .toList();
    }
}
