package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.ReturnResponse;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.ReturnService;
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
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin - Returns", description = "Admin endpoints for inspecting, approving, and rejecting returns")
public class AdminReturnController {

    private final ReturnService returnService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all return requests", description = "Retrieves all customer return requests")
    public ResponseEntity<List<ReturnResponse>> getAllReturns() {
        return ResponseEntity.ok(returnService.getAllReturns());
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve return request", description = "Approves return request and automatically restocks inventory")
    public ResponseEntity<ReturnResponse> approveReturn(
            @PathVariable Long id,
            Authentication authentication) {
        User adminUser = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(returnService.approveReturn(id, adminUser));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject return request", description = "Rejects return request")
    public ResponseEntity<ReturnResponse> rejectReturn(
            @PathVariable Long id,
            Authentication authentication) {
        User adminUser = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(returnService.rejectReturn(id, adminUser));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + email));
    }
}
