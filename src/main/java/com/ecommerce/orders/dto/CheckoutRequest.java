package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.PaymentMethod;
import com.ecommerce.orders.model.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
    @NotBlank(message = "Full name is required") String fullName,
    @NotBlank(message = "Phone number is required") String phone,
    @NotBlank(message = "City is required") String city,
    @NotBlank(message = "Address is required") String addressLine,
    String notes,
    @NotNull(message = "Shipping method is required") ShippingMethod shippingMethod,
    @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
    boolean saveAsDefault
) {}