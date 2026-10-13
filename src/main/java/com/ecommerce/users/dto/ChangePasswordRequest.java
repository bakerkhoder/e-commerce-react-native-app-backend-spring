package com.ecommerce.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    // BCrypt only reads the first 72 bytes; without the cap a longer new password
    // would be silently truncated at the same strength as its first 72 bytes.
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 72, message = "New password must be between 8 and 72 characters")
    String newPassword
) {}