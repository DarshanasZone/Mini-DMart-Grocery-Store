package com.miniproject.minidmart.repository;

import com.miniproject.minidmart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//for search and filtering
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
}

/*

This gives us:

GET all products
GET products by search
GET products by category

*/