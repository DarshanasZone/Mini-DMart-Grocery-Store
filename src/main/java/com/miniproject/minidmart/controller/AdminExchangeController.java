package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.ExchangeResponse;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exchanges")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin - Exchanges", description = "Admin endpoints for inspecting, approving, and rejecting exchanges")
public class AdminExchangeController {

    private final ExchangeService exchangeService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all exchange requests", description = "Retrieves all customer exchange requests")
    public ResponseEntity<List<ExchangeResponse>> getAllExchanges() {
        return ResponseEntity.ok(exchangeService.getAllExchanges());
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve exchange request", description = "Approves exchange, reserves new product inventory, and restocks old item")
    public ResponseEntity<ExchangeResponse> approveExchange(
            @PathVariable Long id,
            Authentication authentication) {
        User adminUser = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(exchangeService.approveExchange(id, adminUser));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject exchange request", description = "Rejects exchange request")
    public ResponseEntity<ExchangeResponse> rejectExchange(
            @PathVariable Long id,
            Authentication authentication) {
        User adminUser = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(exchangeService.rejectExchange(id, adminUser));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + email));
    }
}
