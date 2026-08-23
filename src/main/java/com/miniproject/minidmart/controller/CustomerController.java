package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.entity.Product;
import com.miniproject.minidmart.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ProductService productService;

    // Get all active products
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // Get product by ID
    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // Search products by name
    @GetMapping("/products/search")
    public List<Product> searchProducts(
            @RequestParam String name) {

        return productService.searchProducts(name);
    }

    // Get products by category
    @GetMapping("/products/category/{categoryId}")
    public List<Product> getProductsByCategory(
            @PathVariable Long categoryId) {

        return productService.getProductsByCategory(categoryId);
    }
}