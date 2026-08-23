package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.ReturnRequestDto;
import com.miniproject.minidmart.dto.ReturnResponse;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/returns")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Customer - Returns", description = "Customer endpoints for submitting and viewing returns")
public class CustomerReturnController {

    private final ReturnService returnService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Submit return request", description = "Requests a return for a delivered order")
    public ResponseEntity<ReturnResponse> createReturn(
            Authentication authentication,
            @Valid @RequestBody ReturnRequestDto request) {
        User user = getAuthenticatedUser(authentication);
        ReturnResponse response = returnService.createReturnRequest(user.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get my returns", description = "Retrieves all return requests submitted by customer")
    public ResponseEntity<List<ReturnResponse>> getMyReturns(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(returnService.getReturnsByUser(user.getId()));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
