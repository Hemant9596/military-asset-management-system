package com.kristalball.military.dto;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long baseId,
        String baseName,
        Long assetId,
        String assetName,
        String equipmentTypeName,
        Integer totalQuantity,
        Integer availableQuantity,
        Integer assignedQuantity,
        Integer expendedQuantity,
        LocalDateTime lastUpdated) {
}
