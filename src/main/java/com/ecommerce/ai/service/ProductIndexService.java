package com.ecommerce.ai.service;

import com.ecommerce.catalog.model.Product;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ProductIndexService {

    private final VectorStore vectorStore;

    public ProductIndexService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void indexProduct(Product product) {
        // Combine name + description + attributes into one text blob to embed —
        // richer text means better semantic matches
        String content = product.getName() + ". " + product.getDescription()
            + (product.getCategory() != null ? ". Category: " + product.getCategory().getName() : "");

        Document doc = new Document(
            content,
            Map.of(
                "productId", product.getId(),
                "name", product.getName(),
                "price", product.getPrice().toString()
            )
        );

        vectorStore.add(List.of(doc));
    }
}