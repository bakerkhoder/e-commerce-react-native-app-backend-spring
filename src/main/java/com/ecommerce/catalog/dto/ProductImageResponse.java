package com.ecommerce.catalog.dto;

import com.ecommerce.catalog.model.ProductImage;

public record ProductImageResponse(Long id, String imageUrl, String thumbnailUrl) {
    public static ProductImageResponse from(ProductImage img) {
        return new ProductImageResponse(img.getId(), img.getImageUrl(), img.getThumbnailUrl());
    }
}