package com.ecommerce.users.dto;

import jakarta.validation.constraints.NotBlank;

public record SellerApplicationRequest(@NotBlank(message = "Business name is required") String businessName) {}