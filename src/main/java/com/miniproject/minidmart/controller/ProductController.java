package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.entity.Product;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.AuditService;
import com.miniproject.minidmart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin - Products", description = "Admin product management endpoints")
public class ProductController {

    private final ProductService productService;
    private final AuditService auditService;
    private final UserRepository userRepository;

    // Get all products (active and inactive)
    @GetMapping
    @Operation(summary = "Get all products (Admin)", description = "Retrieves all products in the database")
    public ResponseEntity<List<Product>> getAllProductsAdmin() {
        return ResponseEntity.ok(productService.getAllProductsAdmin());
    }

    // Create product
    @PostMapping
    @Operation(summary = "Create product", description = "Creates a new grocery product")
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            Authentication authentication) {
        Product savedProduct = productService.createProduct(product);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_CREATED_PRODUCT", "Product", savedProduct.getId(),
                "Created product: " + savedProduct.getName() + " (₹" + savedProduct.getPrice() + ")");
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Update product
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates details of a product")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            Authentication authentication) {
        Product updatedProduct = productService.updateProduct(id, product);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_UPDATED_PRODUCT", "Product", updatedProduct.getId(),
                "Updated product: " + updatedProduct.getName() + " (Stock: " + updatedProduct.getStockQuantity() + ")");
        return ResponseEntity.ok(updatedProduct);
    }

    // Toggle active status
    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle product active status", description = "Activates or deactivates product")
    public ResponseEntity<Product> toggleProductStatus(
            @PathVariable Long id,
            Authentication authentication) {
        Product toggled = productService.toggleProductStatus(id);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_TOGGLED_PRODUCT_STATUS", "Product", toggled.getId(),
                "Set product " + toggled.getName() + " active status to: " + toggled.getActive());
        return ResponseEntity.ok(toggled);
    }

    // Deactivate product
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate product", description = "Deactivates a product from the active catalog")
    public ResponseEntity<String> deactivateProduct(
            @PathVariable Long id,
            Authentication authentication) {
        productService.deactivateProduct(id);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_DEACTIVATED_PRODUCT", "Product", id, "Deactivated product ID: " + id);
        return ResponseEntity.ok("Product deactivated successfully");
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}