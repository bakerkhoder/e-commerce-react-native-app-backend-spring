package com.ecommerce.catalog;

import com.ecommerce.ai.service.ProductIndexService;
import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.users.model.Role;
import com.ecommerce.users.model.User;
import com.ecommerce.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final ProductService productService;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepo, ProductService productService,
            UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.categoryRepo = categoryRepo;
        this.productService = productService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCatalog();
    }

    private void seedAdmin() {
        if (userRepo.findByEmail("admin@test.com").isPresent())
            return;

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("admin1234"));
        admin.setFullName("Admin");
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);
        System.out.println("Seeded admin user: admin@test.com / admin1234");
    }

    private void seedCatalog() {
        if (!productService.getAll().isEmpty())
            return;

        Category vegetables = categoryRepo.save(new Category("Vegetables", "grocery"));
        Category electronics = categoryRepo.save(new Category("Electronics", "general"));
        Category supermarket = categoryRepo.save(new Category("Supermarket", "grocery"));

        productService.create(new ProductRequest("Fresh Tomatoes", "Locally grown vine tomatoes",
                new BigDecimal("2.50"), vegetables.getId(), 200, "kg",
                Map.of("organic", true, "origin", "local farm")));

        productService.create(new ProductRequest("Wireless Headphones", "Noise-cancelling over-ear headphones",
                new BigDecimal("89.99"), electronics.getId(), 30, "piece",
                Map.of("brand", "Sony", "warrantyMonths", 24, "color", "black")));

        productService.create(new ProductRequest("Whole Milk", "Fresh pasteurized whole milk",
                new BigDecimal("1.80"), supermarket.getId(), 150, "liter",
                Map.of("fatContent", "3.5%", "brand", "LocalDairy")));

        System.out.println("Seeded 3 categories and 3 products (indexed via events).");
    }
}