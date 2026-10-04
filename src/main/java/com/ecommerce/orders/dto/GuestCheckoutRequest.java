package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.PaymentMethod;
import com.ecommerce.orders.model.ShippingMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record GuestCheckoutRequest(
    @NotBlank @Email String email,
    @NotBlank String fullName,
    @NotBlank String phone,
    @NotBlank String city,
    @NotBlank String addressLine,
    String notes,
    @NotNull ShippingMethod shippingMethod,
    @NotNull PaymentMethod paymentMethod,
    @NotEmpty List<@Valid GuestCheckoutItem> items
) {}