package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.dto.ProductResponse;
import com.ecommerce.catalog.service.ProductService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> getAll(@RequestParam(required = false) Long categoryId) {
        return service.getAll(categoryId).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable Long id) {
        return ProductResponse.from(service.getById(id));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest request,
            @AuthenticationPrincipal Long userId,
            org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ProductResponse.from(service.update(id, request, userId, isAdmin));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        service.delete(id, userId, isAdmin);
    }

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request,
            @AuthenticationPrincipal Long userId,
            org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ProductResponse.from(service.create(request, isAdmin ? null : userId, isAdmin));
    }

    @GetMapping("/mine")
    public List<ProductResponse> myProducts(@AuthenticationPrincipal Long userId) {
        return service.getMyProducts(userId).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/pending")
    public List<ProductResponse> pending() {
        return service.getPendingApprovals().stream().map(ProductResponse::from).toList();
    }

    @PutMapping("/{id}/approve")
    public ProductResponse approve(@PathVariable Long id) {
        return ProductResponse.from(service.setApprovalStatus(id, com.ecommerce.catalog.model.ProductStatus.APPROVED));
    }

    @PutMapping("/{id}/reject")
    public ProductResponse reject(@PathVariable Long id) {
        return ProductResponse.from(service.setApprovalStatus(id, com.ecommerce.catalog.model.ProductStatus.REJECTED));
    }

    @PostMapping("/reindex")
    public java.util.Map<String, Integer> reindex() {
        return java.util.Map.of("indexed", service.reindexAll());
    }

    @PostMapping(value = "/{id}/images", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse addImage(@PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ProductResponse.from(service.addImage(id, file));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ProductResponse removeImage(@PathVariable Long id, @PathVariable Long imageId) {
        return ProductResponse.from(service.removeImage(id, imageId));
    }
}