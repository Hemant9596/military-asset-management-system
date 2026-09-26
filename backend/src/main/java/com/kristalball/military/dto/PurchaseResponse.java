package com.kristalball.military.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        Long baseId,
        String baseName,
        Long assetId,
        String assetName,
        Integer quantity,
        LocalDate purchaseDate,
        String referenceNumber,
        String vendor,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String notes,
        String createdBy,
        LocalDateTime createdAt) {
}
