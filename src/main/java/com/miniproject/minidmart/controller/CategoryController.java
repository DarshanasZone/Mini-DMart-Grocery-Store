package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.entity.Category;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.AuditService;
import com.miniproject.minidmart.service.CategoryService;
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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin - Categories", description = "Admin category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create category", description = "Creates a new category department")
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category,
            Authentication authentication) {
        Category saved = categoryService.createCategory(category);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_CREATED_CATEGORY", "Category", saved.getId(), "Created category: " + saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(summary = "Get all categories (Admin)", description = "Retrieves all categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves category by ID")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates category details")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category,
            Authentication authentication) {
        Category updated = categoryService.updateCategory(id, category);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_UPDATED_CATEGORY", "Category", updated.getId(), "Updated category: " + updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {
        categoryService.deleteCategory(id);
        User adminUser = getAuthenticatedUser(authentication);
        auditService.log(adminUser, "ADMIN_DELETED_CATEGORY", "Category", id, "Deleted category ID: " + id);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}