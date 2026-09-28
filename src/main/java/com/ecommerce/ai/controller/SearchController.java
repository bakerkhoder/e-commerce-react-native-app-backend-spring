package com.ecommerce.ai.controller;

import com.ecommerce.ai.service.SearchService;
import com.ecommerce.catalog.dto.ProductResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<ProductResponse> search(@RequestParam String q) {
        return searchService.searchProducts(q, 10).stream().map(ProductResponse::from).toList();
    }
}