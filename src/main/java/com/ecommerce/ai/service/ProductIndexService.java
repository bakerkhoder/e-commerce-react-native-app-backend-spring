package com.ecommerce.ai.service;

import com.ecommerce.catalog.event.ProductSavedEvent;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductIndexService {

    private final VectorStore vectorStore;

    public ProductIndexService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // Same product always maps to the same vector row: re-indexing overwrites,
    // deleting is exact
    private String documentId(Long productId) {
        return UUID.nameUUIDFromBytes(("product-" + productId).getBytes(StandardCharsets.UTF_8)).toString();
    }

    public void index(ProductSavedEvent p) {
        StringBuilder content = new StringBuilder(p.name());
        if (p.description() != null && !p.description().isBlank()) {
            content.append(". ").append(p.description());
        }
        content.append(". Category: ").append(p.categoryName());
        if (p.attributes() != null && !p.attributes().isEmpty()) {
            content.append(". ");
            p.attributes().forEach((k, v) -> content.append(k).append(' ').append(v).append(". "));
        }

        Document doc = new Document(
                documentId(p.id()),
                content.toString(),
                Map.of("productId", p.id(), "name", p.name(), "price", p.price()));
        vectorStore.add(List.of(doc));
    }

    public void remove(Long productId) {
        vectorStore.delete(List.of(documentId(productId)));
    }
}