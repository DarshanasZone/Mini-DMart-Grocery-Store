package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.OrderRequest;
import com.miniproject.minidmart.dto.OrderResponse;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Customer - Orders", description = "Customer endpoints for checkout, order history, and cancellation")
public class CustomerOrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Place order", description = "Converts current customer cart items into a placed order and reduces stock")
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request) {
        User user = getAuthenticatedUser(authentication);
        OrderResponse response = orderService.placeOrder(user.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get my orders", description = "Retrieves all orders placed by the current customer")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(orderService.getOrdersByUser(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details", description = "Retrieves details of a specific order")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
        return ResponseEntity.ok(orderService.getOrderById(id, user.getId(), isAdmin));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels a placed or confirmed order and restores product stock")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(orderService.cancelOrder(id, user.getId()));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
