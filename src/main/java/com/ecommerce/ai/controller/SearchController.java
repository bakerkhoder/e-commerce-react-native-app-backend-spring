package com.ecommerce.ai.controller;

import com.ecommerce.ai.service.SearchService;
import org.springframework.ai.document.Document;
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
    public List<Document> search(@RequestParam String q) {
        return searchService.search(q, 5);
    }
}