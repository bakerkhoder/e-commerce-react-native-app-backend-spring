package com.ecommerce.analytics;

import com.ecommerce.catalog.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductViewService {

    private static final Logger log = LoggerFactory.getLogger(ProductViewService.class);

    private final ProductViewRepository repository;
    private final ProductService productService;

    public ProductViewService(ProductViewRepository repository, ProductService productService) {
        this.repository = repository;
        this.productService = productService;
    }

    /**
     * Fire-and-forget: a view must never slow down or break a product page, so this runs
     * off the request thread and swallows its own failures.
     */
    @Async("taskExecutor")
    public void record(Long productId) {
        try {
            if (productService.exists(productId)) {
                repository.increment(productId, LocalDate.now());
            }
        } catch (Exception e) {
            log.debug("View not recorded: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> viewsSince(LocalDate from) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : repository.totalsSince(from)) {
            result.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }
}
