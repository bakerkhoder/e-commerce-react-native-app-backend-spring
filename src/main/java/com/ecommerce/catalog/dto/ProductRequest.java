package com.ecommerce.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record ProductRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 2000) String description,
    @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
    @NotNull Long categoryId,
    @NotNull @Min(0) @Max(1_000_000) Integer stockQuantity,
    @Size(max = 20) String unit,
    @Size(max = 30) Map<String, Object> attributes
) {}
