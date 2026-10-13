// users/dto/RegisterRequest.java
package com.ecommerce.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 254) String email,
    // BCrypt only uses the first 72 bytes, so anything longer is silently ignored
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank @Size(max = 100) String fullName,
    @Size(max = 30) String phone,
    @Size(max = 100) String city,
    @Size(max = 255) String addressLine
) {}
