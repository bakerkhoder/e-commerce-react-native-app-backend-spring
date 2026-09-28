package com.ecommerce.ai.listener;

import com.ecommerce.ai.service.ProductIndexService;
import com.ecommerce.catalog.event.ProductDeletedEvent;
import com.ecommerce.catalog.event.ProductSavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProductIndexListener {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexListener.class);
    private final ProductIndexService indexService;

    public ProductIndexListener(ProductIndexService indexService) {
        this.indexService = indexService;
    }

    // The search index is derived data: if indexing fails, log it, but never fail the product save itself
    @EventListener
    public void onSaved(ProductSavedEvent event) {
        try {
            indexService.index(event);
        } catch (Exception e) {
            log.warn("Could not index product {}: {}", event.id(), e.getMessage());
        }
    }

    @EventListener
    public void onDeleted(ProductDeletedEvent event) {
        try {
            indexService.remove(event.id());
        } catch (Exception e) {
            log.warn("Could not remove product {} from index: {}", event.id(), e.getMessage());
        }
    }
}