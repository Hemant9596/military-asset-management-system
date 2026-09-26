package com.kristalball.military.dto;

import jakarta.validation.constraints.NotBlank;

public record EquipmentTypeRequest(@NotBlank String name, String description) {
}