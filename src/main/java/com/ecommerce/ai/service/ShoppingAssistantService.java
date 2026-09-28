package com.ecommerce.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingAssistantService {

    private final SearchService searchService;

    public ShoppingAssistantService(SearchService searchService) {
        this.searchService = searchService;
    }

    public String ask(String question) {
        List<Document> relevant = searchService.search(question, 4);

        if (relevant.isEmpty()) {
            return "I couldn't find anything matching that in our current catalog.";
        }

        String productList = relevant.stream()
            .map(doc -> "- " + doc.getMetadata().get("name") + " ($" + doc.getMetadata().get("price") + ")")
            .collect(Collectors.joining("\n"));

        return "Here's what I found that might match:\n" + productList;
    }
}