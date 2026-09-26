package com.kristalball.military.dto;

public record AssetResponse(
        Long id,
        String name,
        Long equipmentTypeId,
        String equipmentTypeName,
        String serialNumber,
        String model,
        String unit,
        String description,
        boolean active) {
}
