package com.kristalball.military.dto;

public record BaseResponse(
        Long id,
        String name,
        String location,
        String description,
        boolean active) {
}
