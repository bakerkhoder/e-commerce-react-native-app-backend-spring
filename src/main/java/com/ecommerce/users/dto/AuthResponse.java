// users/dto/AuthResponse.java
package com.ecommerce.users.dto;

import com.ecommerce.users.model.Role;

public record AuthResponse(String token, Long userId, String email, String fullName, Role role) {}