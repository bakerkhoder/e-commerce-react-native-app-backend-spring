package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.dto.CategoryResponse;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return service.getAll().stream().map(CategoryResponse::from).toList();
    }

    @PostMapping
    public CategoryResponse create(@RequestBody Category category) {
        return CategoryResponse.from(service.create(category));
    }
}