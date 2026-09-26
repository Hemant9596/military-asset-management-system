package com.kristalball.military.dto;

import jakarta.validation.constraints.NotBlank;

public record BaseRequest(
        @NotBlank(message = "Base name is required") String name,
        String location,
        String description,
        boolean active) {
}
