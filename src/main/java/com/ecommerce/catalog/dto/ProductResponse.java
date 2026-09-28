package com.ecommerce.catalog.dto;

import com.ecommerce.catalog.model.Product;
import java.math.BigDecimal;
import java.util.Map;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Long categoryId,
    String categoryName,
    Integer stockQuantity,
    String unit,
    Map<String, Object> attributes
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
            p.getId(), p.getName(), p.getDescription(), p.getPrice(),
            p.getCategory() != null ? p.getCategory().getId() : null,
            p.getCategory() != null ? p.getCategory().getName() : null,
            p.getStockQuantity(), p.getUnit(), p.getAttributes()
        );
    }
}