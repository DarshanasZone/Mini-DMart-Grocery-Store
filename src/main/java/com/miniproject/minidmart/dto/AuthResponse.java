package com.miniproject.minidmart.dto;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role
) {
}