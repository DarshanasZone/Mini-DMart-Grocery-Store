package com.miniproject.minidmart.service;

import com.miniproject.minidmart.entity.Category;
import com.miniproject.minidmart.entity.Product;
import com.miniproject.minidmart.repository.CategoryRepository;
import com.miniproject.minidmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Create product
    @Transactional
    public Product createProduct(Product product) {
        Long categoryId = product.getCategory().getId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        product.setCategory(category);
        if (product.getActive() == null) {
            product.setActive(true);
        }

        return productRepository.save(product);
    }

    // Get all active products for customer
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    // Get all products for admin
    @Transactional(readOnly = true)
    public List<Product> getAllProductsAdmin() {
        return productRepository.findAll();
    }

    // Get product by ID
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Search products
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    // Get products by category
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    // Update product
    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getProductById(id);

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategory().getId()));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setCategory(category);
        if (product.getActive() != null) {
            existingProduct.setActive(product.getActive());
        }

        return productRepository.save(existingProduct);
    }

    // Toggle active status
    @Transactional
    public Product toggleProductStatus(Long id) {
        Product product = getProductById(id);
        product.setActive(!Boolean.TRUE.equals(product.getActive()));
        return productRepository.save(product);
    }

    // Deactivate product
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }
}