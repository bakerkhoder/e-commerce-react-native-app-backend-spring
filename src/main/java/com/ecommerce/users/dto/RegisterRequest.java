// users/dto/RegisterRequest.java
package com.ecommerce.users.dto;
public record RegisterRequest(String email, String password, String fullName,
                              String phone, String city, String addressLine) {}