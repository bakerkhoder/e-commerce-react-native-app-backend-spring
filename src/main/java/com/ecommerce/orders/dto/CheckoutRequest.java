package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.PaymentMethod;
import com.ecommerce.orders.model.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
    @NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
    @NotBlank(message = "Phone number is required") @Size(max = 30) String phone,
    @NotBlank(message = "City is required") @Size(max = 100) String city,
    @NotBlank(message = "Address is required") @Size(max = 255) String addressLine,
    @Size(max = 500) String notes,
    @NotNull(message = "Shipping method is required") ShippingMethod shippingMethod,
    @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
    boolean saveAsDefault
) {}
