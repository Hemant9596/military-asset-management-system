package com.kristalball.military.dto;

public record AuthResponse(
        String token,
        UserResponse user) {
}
