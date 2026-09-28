package com.ecommerce.catalog.service;

import com.ecommerce.ai.service.ProductIndexService;
import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ProductIndexService indexService;

    public ProductService(ProductRepository repository,
            CategoryRepository categoryRepository,
            ProductIndexService indexService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.indexService = indexService;
    }

    public List<Product> getAll() {
        return repository.findAll();
    }

    public Product getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public boolean hasStock(Long productId, int quantity) {
        Product product = getById(productId);
        return product.getStockQuantity() != null && product.getStockQuantity() >= quantity;
    }

    public Product create(ProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));

        Product product = new Product();
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCategory(category);
        product.setStockQuantity(req.stockQuantity());
        product.setUnit(req.unit());
        product.setAttributes(req.attributes());

        // Save to PostgreSQL database first to generate the ID
        Product saved = repository.save(product);

        // Index the saved product into the pgvector vector store for semantic search
        indexService.indexProduct(saved);

        return saved;
    }

    public Product update(Long id, ProductRequest req) {
        Product product = getById(id);
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCategory(category);
        product.setStockQuantity(req.stockQuantity());
        product.setUnit(req.unit());
        product.setAttributes(req.attributes());
        Product saved = repository.save(product);
        indexService.indexProduct(saved); // re-index so search reflects the update
        return saved;
    }

    public void delete(Long id) {
        repository.deleteById(id);
        // Note: this doesn't remove the old vector from vector_store — Spring AI's
        // VectorStore
        // doesn't track a document-to-productId deletion path out of the box here. A
        // stale
        // vector pointing at a deleted product is a known limitation worth fixing later
        // (store the vector_store document ID alongside the Product row to enable real
        // deletion).
    }

    public void reduceStock(Long productId, int quantity) {
        Product product = getById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        repository.save(product);
    }
}