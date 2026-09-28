package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, Integer quantity) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity());
    }
}