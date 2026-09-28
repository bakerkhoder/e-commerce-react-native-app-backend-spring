package com.ecommerce.orders.controller;

import com.ecommerce.orders.dto.OrderResponse;
import com.ecommerce.orders.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(@AuthenticationPrincipal Long userId) {
        return OrderResponse.from(orderService.checkout(userId));
    }

    @GetMapping
    public List<OrderResponse> myOrders(@AuthenticationPrincipal Long userId) {
        return orderService.getOrdersForUser(userId).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return OrderResponse.from(orderService.getOrderForUser(userId, id));
    }
}