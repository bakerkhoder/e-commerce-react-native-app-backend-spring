package com.ecommerce.ai.mock;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MockAiConfig {

    private static final int DIMENSIONS = 1536;

    @Bean
    public EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = new ArrayList<>();
                int index = 0;
                for (String text : request.getInstructions()) {
                    embeddings.add(new Embedding(hashToVector(text), index++));
                }
                return new EmbeddingResponse(embeddings);
            }

            @Override
            public float[] embed(Document document) {
                return hashToVector(document.getText());
            }
        };
    }

    private float[] hashToVector(String text) {
        float[] vector = new float[DIMENSIONS];
        for (String word : text.toLowerCase().split("\\W+")) {
            if (word.isBlank()) continue;
            int idx = Math.floorMod(word.hashCode(), DIMENSIONS);
            vector[idx] += 1.0f;
        }
        double norm = 0;
        for (float v : vector) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < DIMENSIONS; i++) vector[i] = (float) (vector[i] / norm);
        }
        return vector;
    }
}