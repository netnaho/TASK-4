package com.pharmaprocure.portal.dto;

import java.math.BigDecimal;

public record ProductCatalogResponse(
    Long id,
    String sku,
    String name,
    BigDecimal unitPrice,
    String unit
) {
}
