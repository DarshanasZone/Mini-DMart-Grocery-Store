package com.miniproject.minidmart.util;

import com.miniproject.minidmart.entity.*;
import com.miniproject.minidmart.enums.Role;
import com.miniproject.minidmart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing Mini-DMart demo users and inventory...");

        seedUsers();
        seedCategoriesAndProducts();

        log.info("Mini-DMart demo data initialization complete.");
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@dmart.com")) {
            User admin = User.builder()
                    .name("DMart Admin")
                    .email("admin@dmart.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .build();
            User savedAdmin = userRepository.save(admin);
            cartRepository.save(Cart.builder().user(savedAdmin).build());
            log.info("Created Admin account: admin@dmart.com / Admin@123");
        }

        if (!userRepository.existsByEmail("customer@dmart.com")) {
            User customer = User.builder()
                    .name("Darshana Customer")
                    .email("customer@dmart.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .role(Role.CUSTOMER)
                    .build();
            User savedCustomer = userRepository.save(customer);
            cartRepository.save(Cart.builder().user(savedCustomer).build());
            log.info("Created Customer account: customer@dmart.com / Customer@123");
        }
    }

    private void seedCategoriesAndProducts() {
        Category dairy = getOrCreateCategory("Dairy & Eggs", "Fresh milk, cheese, butter, yogurt, and farm fresh eggs.");
        Category produce = getOrCreateCategory("Fruits & Vegetables", "Organic farm-fresh produce and greens.");
        Category bakery = getOrCreateCategory("Bakery & Snacks", "Artisanal breads, cookies, chips, and savory snacks.");
        Category beverages = getOrCreateCategory("Beverages", "Juices, soft drinks, teas, and premium coffees.");
        Category personal = getOrCreateCategory("Personal Care", "Soaps, shampoos, skincare, and hygiene essentials.");
        Category household = getOrCreateCategory("Household Essentials", "Detergents, cleaners, disinfectants, and paper towels.");
        Category staples = getOrCreateCategory("Staples & Grains", "Basmati rice, flour, pulses, spices, and cooking oils.");

        // Dairy Products
        createProductIfAbsent("Amul Taaza Toned Milk 1L", "Fresh pasteurized toned milk rich in calcium and protein.",
                new BigDecimal("56.00"), 100, "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80", dairy);
        createProductIfAbsent("Amul Salted Butter 500g", "Delicious creamy butter made from pure cow milk.",
                new BigDecimal("275.00"), 60, "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=600&auto=format&fit=crop&q=80", dairy);
        createProductIfAbsent("Farm Fresh Brown Eggs (Pack of 12)", "Organic, naturally raised antibiotic-free farm eggs.",
                new BigDecimal("110.00"), 80, "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=600&auto=format&fit=crop&q=80", dairy);
        createProductIfAbsent("Britannia Processed Cheese Slices 200g", "Classic rich melted cheese slices for sandwiches.",
                new BigDecimal("140.00"), 45, "https://images.unsplash.com/photo-1624806992066-5ffcf7ca186b?w=600&auto=format&fit=crop&q=80", dairy);

        // Produce Products
        createProductIfAbsent("Fresh Shimla Apples 1kg", "Crisp, juicy and sweet premium red apples from Himachal.",
                new BigDecimal("180.00"), 75, "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600&auto=format&fit=crop&q=80", produce);
        createProductIfAbsent("Fresh Robusta Bananas 1kg", "Naturally ripened sweet bananas packed with potassium.",
                new BigDecimal("45.00"), 120, "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop&q=80", produce);
        createProductIfAbsent("Hybrid Tomatoes 1kg", "Juicy and firm red tomatoes for curries and salads.",
                new BigDecimal("35.00"), 150, "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&auto=format&fit=crop&q=80", produce);
        createProductIfAbsent("Fresh Baby Spinach 250g", "Washed, tender, nutrient-rich green spinach leaves.",
                new BigDecimal("30.00"), 50, "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80", produce);

        // Bakery Products
        createProductIfAbsent("Whole Wheat Brown Bread 400g", "100% whole wheat high-fiber freshly baked loaf.",
                new BigDecimal("45.00"), 50, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop&q=80", bakery);
        createProductIfAbsent("Dark Chocolate Chunk Cookies 200g", "Crunchy oven-baked cookies loaded with rich cocoa chunks.",
                new BigDecimal("120.00"), 40, "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=600&auto=format&fit=crop&q=80", bakery);

        // Beverages
        createProductIfAbsent("Tropicana 100% Orange Juice 1L", "Pure pressed orange juice without added sugar.",
                new BigDecimal("130.00"), 85, "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=600&auto=format&fit=crop&q=80", beverages);
        createProductIfAbsent("Nescafe Classic Instant Coffee 200g", "Rich aroma and bold taste crafted from roasted Robusta beans.",
                new BigDecimal("380.00"), 40, "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop&q=80", beverages);
        createProductIfAbsent("Tata Tea Premium 1kg", "Unique blend of fine tea leaves and granules.",
                new BigDecimal("420.00"), 60, "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80", beverages);

        // Personal Care
        createProductIfAbsent("Dove Deep Moisture Body Wash 500ml", "Gentle nourishing body wash with NutriumMoisture.",
                new BigDecimal("325.00"), 35, "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80", personal);
        createProductIfAbsent("Colgate Total Dental Cream 150g", "Antibacterial toothpaste for complete oral cavity protection.",
                new BigDecimal("115.00"), 90, "https://images.unsplash.com/photo-1588776814546-1ffcf47267a5?w=600&auto=format&fit=crop&q=80", personal);

        // Household
        createProductIfAbsent("Surf Excel Matic Liquid Detergent 2L", "Top load specialized stain removal liquid detergent.",
                new BigDecimal("399.00"), 45, "https://images.unsplash.com/photo-1610557892470-55d9e80c0bce?w=600&auto=format&fit=crop&q=80", household);

        // Staples & Grains
        createProductIfAbsent("Fortune Sunlite Refined Sunflower Oil 1L", "Enriched with vitamins A and D for healthy daily cooking.",
                new BigDecimal("145.00"), 110, "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop&q=80", staples);
        createProductIfAbsent("India Gate Super Basmati Rice 5kg", "Long grain aromatic aged basmati rice for royal biryanis.",
                new BigDecimal("650.00"), 30, "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80", staples);
    }

    private Category getOrCreateCategory(String name, String description) {
        Optional<Category> existing = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        Category category = Category.builder()
                .name(name)
                .description(description)
                .active(true)
                .build();
        return categoryRepository.save(category);
    }

    private void createProductIfAbsent(String name, String description, BigDecimal price, int stock, String imageUrl, Category category) {
        boolean exists = productRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
        if (!exists) {
            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stockQuantity(stock)
                    .imageUrl(imageUrl)
                    .active(true)
                    .category(category)
                    .build();
            productRepository.save(product);
        }
    }
}
