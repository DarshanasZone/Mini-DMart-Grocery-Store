package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.OrderResponse;
import com.miniproject.minidmart.dto.UpdateOrderStatusRequest;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin - Orders", description = "Admin endpoints for viewing and managing orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders in the system")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates order fulfillment status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        User adminUser = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus(), adminUser));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + email));
    }
}
