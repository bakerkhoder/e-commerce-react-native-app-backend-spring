package com.ecommerce.users.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // industry-standard one-way password hashing
    }

    /**
     * Native apps ignore CORS entirely, but an Expo web build enforces it. The
     * allowlist is explicit and empty by default, which means no browser origin
     * is allowed until you name one in CORS_ORIGINS.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:}") List<String> origins) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origins.stream().filter(o -> !o.isBlank()).toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // fine for a stateless JSON API; would differ for a browser-form
                                                       // app
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/guest-checkout").permitAll()
                        .requestMatchers("/api/search/**", "/api/assistant/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        // first match wins, so the public view ping must come before the
                        // broader dashboard rules below
                        .requestMatchers(HttpMethod.POST, "/api/analytics/views/*").permitAll()
                        .requestMatchers("/api/products/pending", "/api/products/*/approve", "/api/products/*/reject")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers("/api/dashboard/**", "/api/ledger/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers("/api/products/**", "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/seller-applications/**").hasRole("ADMIN")
                        .requestMatchers("/api/products/pending", "/api/products/*/approve", "/api/products/*/reject")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/orders/all", "/api/orders/*/status").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
