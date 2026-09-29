package com.ecommerce.catalog.service;

import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.event.ProductDeletedEvent;
import com.ecommerce.catalog.event.ProductSavedEvent;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.common.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher events;

    public ProductService(ProductRepository repository, CategoryRepository categoryRepository,
            ApplicationEventPublisher events) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Product> getAll(Long categoryId) {
        return categoryId == null ? repository.findAll() : repository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    /**
     * Returns products in the same order as the given ids, silently skipping any
     * that no longer exist.
     */
    @Transactional(readOnly = true)
    public List<Product> getByIdsOrdered(List<Long> ids) {
        Map<Long, Product> byId = repository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    public boolean hasStock(Long productId, int quantity) {
        Product product = getById(productId);
        return product.getStockQuantity() != null && product.getStockQuantity() >= quantity;
    }

    @Transactional
    public Product create(ProductRequest req) {
        Product product = new Product();
        apply(product, req);
        Product saved = repository.save(product);
        events.publishEvent(toSavedEvent(saved));
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product product = getById(id);
        apply(product, req);
        Product saved = repository.save(product);
        events.publishEvent(toSavedEvent(saved));
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
        events.publishEvent(new ProductDeletedEvent(id));
    }

    @Transactional
    public void reduceStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        repository.save(product);
    }

    /**
     * Re-publishes every product so listeners (e.g. the search index) can rebuild
     * from scratch.
     */
    public int reindexAll() {
        List<Product> all = repository.findAll();
        all.forEach(p -> events.publishEvent(toSavedEvent(p)));
        return all.size();
    }

    private void apply(Product product, ProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId()));
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCategory(category);
        product.setStockQuantity(req.stockQuantity());
        product.setUnit(req.unit());
        product.setAttributes(req.attributes());
    }

    private ProductSavedEvent toSavedEvent(Product p) {
        return new ProductSavedEvent(
                p.getId(), p.getName(), p.getDescription(),
                p.getPrice().toPlainString(),
                p.getCategory() != null ? p.getCategory().getName() : "",
                p.getAttributes());
    }

    @Transactional(readOnly = true)
    public List<Product> searchByKeyword(String term, int limit) {
        return repository.searchByKeyword(term, PageRequest.of(0, limit));
    }
}