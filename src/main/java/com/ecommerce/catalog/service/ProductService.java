package com.ecommerce.catalog.service;

import com.ecommerce.catalog.config.MarketplaceProperties;
import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.event.ProductDeletedEvent;
import com.ecommerce.catalog.event.ProductSavedEvent;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.model.ProductImage;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.catalog.repository.ProductImageRepository;
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
import com.ecommerce.catalog.model.ProductStatus;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher events;
    private final ProductImageService imageService;
    private final ProductImageRepository imageRepository;
    private final MarketplaceProperties marketplaceProperties;

    // add to constructor
    public ProductService(ProductRepository repository, CategoryRepository categoryRepository,
            ApplicationEventPublisher events, ProductImageService imageService,
            ProductImageRepository imageRepository, MarketplaceProperties marketplaceProperties) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.events = events;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.marketplaceProperties = marketplaceProperties;
    }

    @Transactional(readOnly = true)
    public List<Product> getAll(Long categoryId) {
        // Public browsing only ever sees approved products — this is what keeps a
        // pending
        // or rejected seller listing invisible to customers until an admin acts on it
        List<Product> all = categoryId == null ? repository.findAll() : repository.findByCategoryId(categoryId);
        return all.stream().filter(p -> p.getStatus() == ProductStatus.APPROVED).toList();
    }

    @Transactional(readOnly = true)
    public List<Product> getMyProducts(Long sellerId) {
        return repository.findBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Product> getPendingApprovals() {
        return repository.findByStatus(ProductStatus.PENDING);
    }

    @Transactional
    public Product setApprovalStatus(Long productId, ProductStatus status) {
        Product product = getById(productId);
        product.setStatus(status);
        Product saved = repository.save(product);
        if (status == ProductStatus.APPROVED) {
            events.publishEvent(toSavedEvent(saved)); // becomes searchable the moment it's approved
        }
        return saved;
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
    public Product create(ProductRequest req, Long sellerId, boolean isAdmin) {
        if (marketplaceProperties.isEnabled() && !isAdmin && sellerId == null) {
            throw new IllegalStateException("Only sellers or admins can create products");
        }

        Product product = new Product();
        apply(product, req);

        if (marketplaceProperties.isEnabled()) {
            product.setSellerId(isAdmin ? null : sellerId); // admin-created products stay platform-owned
            product.setStatus(isAdmin ? ProductStatus.APPROVED : ProductStatus.PENDING); // sellers need approval
        } else {
            product.setSellerId(null);
            product.setStatus(ProductStatus.APPROVED); // marketplace off: everything auto-approves, today's behavior
        }

        Product saved = repository.save(product);
        if (saved.getStatus() == ProductStatus.APPROVED) {
            events.publishEvent(toSavedEvent(saved)); // only searchable once approved
        }
        return saved;
    }

    @Transactional
    // add a parameter to both: Long requestingUserId, boolean isAdmin
    public Product update(Long id, ProductRequest req, Long requestingUserId, boolean isAdmin) {
        Product product = getById(id);
        if (!isAdmin && !java.util.Objects.equals(product.getSellerId(), requestingUserId)) {
            throw new IllegalStateException("You can only edit your own products");
        }
        apply(product, req);
        Product saved = repository.save(product);
        if (saved.getStatus() == ProductStatus.APPROVED)
            events.publishEvent(toSavedEvent(saved));
        return saved;
    }

    @Transactional
    public void delete(Long id, Long requestingUserId, boolean isAdmin) {
        Product product = getById(id);
        if (!isAdmin && !java.util.Objects.equals(product.getSellerId(), requestingUserId)) {
            throw new IllegalStateException("You can only delete your own products");
        }
        imageService.deleteFiles(
                product.getImages().stream()
                        .flatMap(img -> java.util.stream.Stream.of(img.getImageUrl(), img.getThumbnailUrl()))
                        .toArray(String[]::new));
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
        String thumb = p.getImages().isEmpty() ? null : p.getImages().get(0).getThumbnailUrl();
        return new ProductSavedEvent(
                p.getId(), p.getName(), p.getDescription(), p.getPrice().toPlainString(),
                p.getCategory() != null ? p.getCategory().getName() : "", p.getAttributes(), thumb);
    }

    @Transactional(readOnly = true)
    public List<Product> searchByKeyword(String term, int limit) {
        return repository.searchByKeyword(term, PageRequest.of(0, limit));
    }

    @Transactional
    public Product addImage(Long productId, org.springframework.web.multipart.MultipartFile file) {
        Product product = getById(productId);
        ProductImageService.ImagePaths paths = imageService.store(productId, file);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(paths.imageUrl());
        image.setThumbnailUrl(paths.thumbnailUrl());
        image.setSortOrder(product.getImages().size()); // append to the end of the gallery
        product.getImages().add(image);

        Product saved = repository.save(product);
        events.publishEvent(toSavedEvent(saved)); // keeps search's thumbnail metadata current
        return saved;
    }

    @Transactional
    public Product removeImage(Long productId, Long imageId) {
        Product product = getById(productId);
        ProductImage toRemove = product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Image not found on this product"));

        imageService.deleteFiles(toRemove.getImageUrl(), toRemove.getThumbnailUrl());
        product.getImages().remove(toRemove); // orphanRemoval deletes the row on save

        Product saved = repository.save(product);
        events.publishEvent(toSavedEvent(saved));
        return saved;
    }

}