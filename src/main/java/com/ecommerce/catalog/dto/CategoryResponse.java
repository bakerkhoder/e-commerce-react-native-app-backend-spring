package com.ecommerce.catalog.dto;

import com.ecommerce.catalog.model.Category;

public record CategoryResponse(Long id, String name, String vertical) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getVertical());
    }
}