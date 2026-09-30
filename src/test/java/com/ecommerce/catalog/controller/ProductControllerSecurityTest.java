package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.service.CategoryService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.users.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Updated import
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    
    @MockitoBean ProductService productService;    // Replaced @MockBean with @MockitoBean
    @MockitoBean CategoryService categoryService; // Replaced @MockBean with @MockitoBean
    @MockitoBean JwtAuthFilter jwtAuthFilter;     // Replaced @MockBean with @MockitoBean

    @Test
    void getProducts_isPublic_evenWithNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk());
    }

    @Test
    void createProduct_isRejected_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/products").contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createProduct_isForbidden_forNonAdminUser() throws Exception {
        mockMvc.perform(post("/api/products").contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_isAllowed_forAdminUser() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content("""
                    {"name":"Test","description":"d","price":1.0,"categoryId":1,"stockQuantity":1,"unit":"piece","attributes":{}}
                    """))
            .andExpect(status().isOk());
    }
}