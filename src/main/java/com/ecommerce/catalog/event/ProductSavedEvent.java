package com.ecommerce.catalog.event;

import java.util.Map;

public record ProductSavedEvent(Long id, String name, String description, String price,
                                String categoryName, Map<String, Object> attributes, String thumbnailUrl) {}