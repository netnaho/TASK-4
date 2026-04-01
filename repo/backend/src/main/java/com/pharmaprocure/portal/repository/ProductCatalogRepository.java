package com.pharmaprocure.portal.repository;

import com.pharmaprocure.portal.entity.ProductCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCatalogRepository extends JpaRepository<ProductCatalogEntity, Long> {
}
