package com.kristalball.military.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OpeningBalanceResponse(
        Long id,
        Long baseId,
        String baseName,
        Long assetId,
        String assetName,
        Integer quantity,
        LocalDate effectiveDate,
        String createdBy,
        LocalDateTime createdAt) {
}