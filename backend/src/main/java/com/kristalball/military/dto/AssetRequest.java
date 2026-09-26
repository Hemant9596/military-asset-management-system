package com.kristalball.military.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
        @NotBlank(message = "Asset name is required") String name,
        @NotNull(message = "Equipment type is required") Long equipmentTypeId,
        String serialNumber,
        String model,
        String unit,
        String description,
        boolean active) {
}
