package com.ecommerce.common;

import org.springframework.security.core.Authentication;

/** Shared authority check; every admin-capable controller used to inline this. */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
