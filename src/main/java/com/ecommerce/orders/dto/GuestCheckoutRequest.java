package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.PaymentMethod;
import com.ecommerce.orders.model.ShippingMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record GuestCheckoutRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank @Size(max = 30) String phone,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 255) String addressLine,
    @Size(max = 500) String notes,
    @NotNull ShippingMethod shippingMethod,
    @NotNull PaymentMethod paymentMethod,
    @NotEmpty @Size(max = 50) List<@Valid GuestCheckoutItem> items
) {}
