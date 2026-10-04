package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.SetQuantityRequest;
import com.ecommerce.cart.service.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal Long userId) {
        return CartResponse.from(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal Long userId,
                                 @RequestParam Long productId,
                                 @RequestParam int quantity) {
        return CartResponse.from(cartService.addItem(userId, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal Long userId, @PathVariable Long productId) {
        return CartResponse.from(cartService.removeItem(userId, productId));
    }

    @PutMapping("/items/{productId}")
    public CartResponse setQuantity(@AuthenticationPrincipal Long userId, @PathVariable Long productId,
                                @RequestBody SetQuantityRequest request) {
     return CartResponse.from(cartService.setItemQuantity(userId, productId, request.quantity()));
    }
}