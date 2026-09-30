package com.ecommerce.catalog.service;

import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.event.ProductSavedEvent;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductImageService imageService;
    @Mock com.ecommerce.catalog.repository.ProductImageRepository imageRepository;
    @Mock ApplicationEventPublisher events;

    ProductService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new ProductService(productRepository, categoryRepository, events, imageService, imageRepository);
    }

    @Test
    void create_publishesProductSavedEvent_soSearchIndexStaysInSync() {
        Category category = new Category("Vegetables", "grocery");
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(42L);
            return p;
        });

        ProductRequest request = new ProductRequest(
            "Tomatoes", "Fresh", new BigDecimal("2.50"), 1L, 100, "kg", Map.of("organic", true));

        service.create(request);

        verify(events).publishEvent(any(ProductSavedEvent.class));
    }

    @Test
    void getById_throwsNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_throwsIllegalArgument_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ProductRequest request = new ProductRequest(
            "Tomatoes", "Fresh", new BigDecimal("2.50"), 1L, 100, "kg", Map.of());

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reduceStock_throwsIllegalState_whenRequestedQuantityExceedsStock() {
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.reduceStock(1L, 10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void reduceStock_decrementsCorrectly_whenStockIsSufficient() {
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        service.reduceStock(1L, 3);

        assertThat(product.getStockQuantity()).isEqualTo(7);
    }
}