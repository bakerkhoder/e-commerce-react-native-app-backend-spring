package com.ecommerce.orders.event;

import com.ecommerce.orders.model.Order;

public record OrderPlacedEvent(Order order, String customerEmail) {}