package com.ecommerce.ai.service;

import com.ecommerce.catalog.model.Product;
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
        List<Product> matches = searchService.searchProducts(question, 5,null);

        if (matches.isEmpty()) {
            return "I couldn't find anything matching that in our current catalog.";
        }

        String list = matches.stream()
            .map(p -> "- " + p.getName()
                + " ($" + p.getPrice().toPlainString() + (p.getUnit() != null ? " / " + p.getUnit() : "") + ", "
                + (p.getStockQuantity() != null && p.getStockQuantity() > 0 ? "in stock" : "out of stock") + ")")
            .collect(Collectors.joining("\n"));

        return "Here's what I found that might match:\n" + list;
    }
}