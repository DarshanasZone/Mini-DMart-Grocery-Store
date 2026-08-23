package com.miniproject.minidmart.repository;

import com.miniproject.minidmart.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}