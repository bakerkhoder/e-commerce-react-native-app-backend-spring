package com.ecommerce.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** quantity <= 0 removes the line; 1..99 sets it. */
public record SetQuantityRequest(@Min(-1) @Max(99) int quantity) {}
