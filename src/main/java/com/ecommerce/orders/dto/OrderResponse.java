package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.Order;
import com.ecommerce.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
    Long id, Long userId, List<OrderItemResponse> items,
    BigDecimal totalAmount, OrderStatus status, Instant createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
            o.getId(), o.getUserId(),
            o.getItems().stream().map(OrderItemResponse::from).toList(),
            o.getTotalAmount(), o.getStatus(), o.getCreatedAt()
        );
    }
}