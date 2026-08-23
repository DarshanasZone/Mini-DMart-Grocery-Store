package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.ExchangeRequestDto;
import com.miniproject.minidmart.dto.ExchangeResponse;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.UserRepository;
import com.miniproject.minidmart.service.ExchangeService;
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
@RequestMapping("/api/customer/exchanges")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Customer - Exchanges", description = "Customer endpoints for submitting and viewing exchanges")
public class CustomerExchangeController {

    private final ExchangeService exchangeService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Submit exchange request", description = "Requests an exchange of a delivered item for a new product")
    public ResponseEntity<ExchangeResponse> createExchange(
            Authentication authentication,
            @Valid @RequestBody ExchangeRequestDto request) {
        User user = getAuthenticatedUser(authentication);
        ExchangeResponse response = exchangeService.createExchangeRequest(user.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get my exchanges", description = "Retrieves all exchange requests submitted by customer")
    public ResponseEntity<List<ExchangeResponse>> getMyExchanges(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(exchangeService.getExchangesByUser(user.getId()));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
