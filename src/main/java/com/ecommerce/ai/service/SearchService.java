package com.ecommerce.ai.service;

import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    // Filler words that would otherwise match random descriptions when a user types
    // a sentence
    private static final Set<String> STOPWORDS = Set.copyOf(List.of(
            "a", "an", "the", "and", "or", "of", "to", "in", "on", "for", "with", "do", "does",
            "you", "your", "have", "has", "any", "anything", "some", "what", "which", "that",
            "this", "are", "is", "it", "can", "get", "sell", "carry", "there", "i", "me", "we",
            "my", "show", "find", "want", "need", "looking", "please"));

    private final VectorStore vectorStore;
    private final ProductService productService;

    public SearchService(VectorStore vectorStore, ProductService productService) {
        this.vectorStore = vectorStore;
        this.productService = productService;
    }

    public List<Document> search(String query, int limit) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(limit).similarityThreshold(0.05).build());
    }

    /**
     * Hybrid search: keyword matches (partial words, exact database truth) plus
     * semantic matches.
     * Results are always resolved through the database, so deleted products can
     * never appear.
     */
    public List<Product> searchProducts(String query, int limit, Long categoryId) {
        List<String> tokens = tokenize(query);
        if (tokens.isEmpty())
            return List.of();

        Map<Long, Product> byId = new LinkedHashMap<>();
        Map<Long, Integer> score = new HashMap<>();

        for (String token : tokens) {
            for (Product p : productService.searchByKeyword(token, limit * 2)) {
                byId.putIfAbsent(p.getId(), p);
                score.merge(p.getId(), 2, Integer::sum);
            }
        }

        try {
            List<Long> ids = search(query, limit * 2).stream()
                    .map(d -> ((Number) d.getMetadata().get("productId")).longValue())
                    .distinct()
                    .toList();
            for (Product p : productService.getByIdsOrdered(ids)) {
                byId.putIfAbsent(p.getId(), p);
                score.merge(p.getId(), 1, Integer::sum);
            }
        } catch (Exception e) {
            log.warn("Semantic search unavailable, returning keyword results only: {}", e.getMessage());
        }

        return byId.values().stream()
                .filter(p -> categoryId == null
                        || (p.getCategory() != null && p.getCategory().getId().equals(categoryId)))
                .sorted(Comparator.comparingInt((Product p) -> score.get(p.getId())).reversed())
                .limit(limit)
                .toList();
    }

    // Splits on anything that isn't a letter or digit (Unicode-aware, so Arabic
    // works too)
    private List<String> tokenize(String query) {
        if (query == null)
            return List.of();
        return Arrays.stream(query.toLowerCase().split("[^\\p{L}\\p{N}]+"))
                .filter(t -> t.length() >= 2 && !STOPWORDS.contains(t))
                .distinct()
                .limit(5)
                .toList();
    }
}