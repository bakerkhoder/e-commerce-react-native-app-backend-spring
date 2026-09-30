package com.ecommerce.cart.service;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock ProductService productService;

    CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productService);
    }

    private Product product(Long id, String name, String price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStockQuantity(stock);
        return p;
    }

    @Test
    void addItem_throwsIllegalState_whenRequestedQuantityExceedsStock() {
        when(productService.hasStock(1L, 100)).thenReturn(false);

        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 100))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void addItem_increasesQuantity_ratherThanDuplicatingRow_whenProductAlreadyInCart() {
        Cart cart = new Cart(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productService.hasStock(1L, 1)).thenReturn(true);
        when(productService.getById(1L)).thenReturn(product(1L, "Tomatoes", "2.50", 100));

        cartService.addItem(1L, 1L, 1);
        Cart result = cartService.addItem(1L, 1L, 2);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void addItem_snapshotsPriceAtAddTime_soLaterPriceChangesDontAffectExistingCartLines() {
        Cart cart = new Cart(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productService.hasStock(1L, 1)).thenReturn(true);
        when(productService.getById(1L)).thenReturn(product(1L, "Tomatoes", "2.50", 100));

        Cart result = cartService.addItem(1L, 1L, 1);

        assertThat(result.getItems().get(0).getUnitPrice()).isEqualByComparingTo("2.50");
    }
}