package com.ecommerce.users.dto;

import com.ecommerce.users.model.Role;
import com.ecommerce.users.model.User;

public record UserResponse(Long userId, String email, String fullName, Role role,
                           String defaultPhone, String defaultCity, String defaultAddressLine) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(),
            u.getDefaultPhone(), u.getDefaultCity(), u.getDefaultAddressLine());
    }
}