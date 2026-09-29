package com.ecommerce.catalog.dto;

import com.ecommerce.catalog.model.Product;

import java.math.BigDecimal;
import java.util.List;
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
        Map<String, Object> attributes,
        List<ProductImageResponse> images,
        String thumbnailUrl) {
    public static ProductResponse from(Product p) {
        List<ProductImageResponse> images = p.getImages().stream().map(ProductImageResponse::from).toList();

        return new ProductResponse(

                p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getStockQuantity(),
                p.getUnit(),
                p.getAttributes(),
                images,
                images.isEmpty() ? null : images.get(0).thumbnailUrl() // convenience for card views
        );
    }
}