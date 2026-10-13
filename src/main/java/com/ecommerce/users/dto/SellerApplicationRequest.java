package com.ecommerce.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerApplicationRequest(
    @NotBlank(message = "Business name is required")
    @Size(max = 100, message = "Business name is too long")
    String businessName
) {}