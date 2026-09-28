package com.ecommerce.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchService {

    private final VectorStore vectorStore;

    public SearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> search(String query, int limit) {
        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(limit)
                .similarityThreshold(0.05) // low, because our mock embedder produces weaker scores than a real model
                .build()
        );
    }
}