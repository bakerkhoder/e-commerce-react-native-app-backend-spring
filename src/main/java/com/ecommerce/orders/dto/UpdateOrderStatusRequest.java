package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {}