package com.kristalball.military.dto;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        Long assignedBaseId,
        String assignedBaseName,
        boolean enabled) {
}
