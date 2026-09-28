package com.ecommerce.catalog;

import com.ecommerce.ai.service.ProductIndexService;
import com.ecommerce.catalog.model.Category;
import com.ecommerce.catalog.model.Product;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
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
    private final ProductRepository productRepo;
    private final ProductIndexService indexService;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepo, ProductRepository productRepo,
                       ProductIndexService indexService, UserRepository userRepo,
                       PasswordEncoder passwordEncoder) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.indexService = indexService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCatalog();
    }

    private void seedAdmin() {
        if (userRepo.findByEmail("admin@test.com").isPresent()) return;

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("admin1234"));
        admin.setFullName("Admin");
        admin.setRole(Role.ADMIN);
        userRepo.save(admin);
        System.out.println("Seeded admin user: admin@test.com / admin1234");
    }

    private void seedCatalog() {
        if (productRepo.count() > 0) return; // don't reseed products on every restart

        Category vegetables = categoryRepo.save(new Category("Vegetables", "grocery"));
        Category electronics = categoryRepo.save(new Category("Electronics", "general"));
        Category supermarket = categoryRepo.save(new Category("Supermarket", "grocery"));

        Product tomato = new Product();
        tomato.setName("Fresh Tomatoes");
        tomato.setDescription("Locally grown vine tomatoes");
        tomato.setPrice(new BigDecimal("2.50"));
        tomato.setCategory(vegetables);
        tomato.setUnit("kg");
        tomato.setStockQuantity(200);
        tomato.setAttributes(Map.of("organic", true, "origin", "local farm"));
        Product savedTomato = productRepo.save(tomato);
        indexService.indexProduct(savedTomato);

        Product headphones = new Product();
        headphones.setName("Wireless Headphones");
        headphones.setDescription("Noise-cancelling over-ear headphones");
        headphones.setPrice(new BigDecimal("89.99"));
        headphones.setCategory(electronics);
        headphones.setUnit("piece");
        headphones.setStockQuantity(30);
        headphones.setAttributes(Map.of("brand", "Sony", "warrantyMonths", 24, "color", "black"));
        Product savedHeadphones = productRepo.save(headphones);
        indexService.indexProduct(savedHeadphones);

        Product milk = new Product();
        milk.setName("Whole Milk");
        milk.setDescription("Fresh pasteurized whole milk");
        milk.setPrice(new BigDecimal("1.80"));
        milk.setCategory(supermarket);
        milk.setUnit("liter");
        milk.setStockQuantity(150);
        milk.setAttributes(Map.of("fatContent", "3.5%", "brand", "LocalDairy"));
        Product savedMilk = productRepo.save(milk);
        indexService.indexProduct(savedMilk);

        System.out.println("Seeded 3 categories, 3 products, and indexed them into vector_store.");
    }
}