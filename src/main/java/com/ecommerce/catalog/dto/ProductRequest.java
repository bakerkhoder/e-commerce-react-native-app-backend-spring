package com.ecommerce.catalog.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ProductRequest(
    String name,
    String description,
    BigDecimal price,
    Long categoryId,
    Integer stockQuantity,
    String unit,
    Map<String, Object> attributes
) {}