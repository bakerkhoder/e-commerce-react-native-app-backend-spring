package com.ecommerce.cart.service;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService; // the only door into catalog

    public CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));
    }

    public Cart addItem(Long userId, Long productId, int quantity) {
        if (!productService.hasStock(productId, quantity)) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }

        Product product = productService.getById(productId);
        Cart cart = getOrCreateCart(userId);

        // If it's already in the cart, increase quantity instead of duplicating the row
        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + quantity),
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setCart(cart);
                            newItem.setProductId(product.getId());
                            newItem.setProductName(product.getName());
                            newItem.setUnitPrice(product.getPrice());
                            newItem.setQuantity(quantity);
                            cart.getItems().add(newItem);
                        });

        return cartRepository.save(cart);
    }

    public Cart removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    // add to CartService.java
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional
    public Cart setItemQuantity(Long userId, Long productId, int quantity) {
     if (quantity <= 0) {
         return removeItem(userId, productId);
        }
     if (!productService.hasStock(productId, quantity)) {
         throw new IllegalStateException("Insufficient stock for that quantity");
       }
     Cart cart = getOrCreateCart(userId);
     CartItem item = cart.getItems().stream()
        .filter(i -> i.getProductId().equals(productId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Item not in cart"));
     item.setQuantity(quantity);
     return cartRepository.save(cart);
    }


}