package com.ecommerce.orders.dto;

import com.ecommerce.orders.model.ShippingAddress;

public record ShippingAddressResponse(String fullName, String phone, String country, String city, String addressLine, String notes) {
    public static ShippingAddressResponse from(ShippingAddress a) {
        return new ShippingAddressResponse(a.getFullName(), a.getPhone(), a.getCountry(), a.getCity(), a.getAddressLine(), a.getNotes());
    }
}