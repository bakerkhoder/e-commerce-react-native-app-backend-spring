package com.ecommerce.cart.dto;

import com.ecommerce.cart.model.CartItem;
import java.math.BigDecimal;

public record CartItemResponse(
    Long productId,
    String productName,
    BigDecimal unitPrice,
    Integer quantity
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
            item.getProductId(),
            item.getProductName(),
            item.getUnitPrice(),
            item.getQuantity()
        );
    }
}