package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.entity.Cart;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Customer - Cart", description = "Customer shopping cart endpoints")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    // Get cart
    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves items and total for the customer's cart")
    public ResponseEntity<Cart> getCart(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        Long resolvedUserId = resolveUserId(authentication, userId);
        return ResponseEntity.ok(cartService.getCart(resolvedUserId));
    }

    // Add product to cart
    @PostMapping("/add")
    @Operation(summary = "Add product to cart", description = "Adds or increments product in cart")
    public ResponseEntity<Cart> addToCart(
            Authentication authentication,
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Long resolvedUserId = resolveUserId(authentication, userId);
        return ResponseEntity.ok(cartService.addToCart(resolvedUserId, productId, quantity));
    }

    // Update quantity
    @PutMapping("/update")
    @Operation(summary = "Update quantity", description = "Sets product quantity in cart")
    public ResponseEntity<Cart> updateQuantity(
            Authentication authentication,
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Long resolvedUserId = resolveUserId(authentication, userId);
        return ResponseEntity.ok(cartService.updateQuantity(resolvedUserId, productId, quantity));
    }

    // Remove product
    @DeleteMapping("/remove")
    @Operation(summary = "Remove from cart", description = "Removes a product from cart")
    public ResponseEntity<Cart> removeFromCart(
            Authentication authentication,
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId) {
        Long resolvedUserId = resolveUserId(authentication, userId);
        return ResponseEntity.ok(cartService.removeFromCart(resolvedUserId, productId));
    }

    // Clear cart
    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart", description = "Empties all items from cart")
    public ResponseEntity<String> clearCart(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        Long resolvedUserId = resolveUserId(authentication, userId);
        cartService.clearCart(resolvedUserId);
        return ResponseEntity.ok("Cart cleared successfully");
    }

    private Long resolveUserId(Authentication authentication, Long explicitUserId) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                return user.getId();
            }
        }
        if (explicitUserId != null) {
            return explicitUserId;
        }
        throw new RuntimeException("Authenticated user not found");
    }
}