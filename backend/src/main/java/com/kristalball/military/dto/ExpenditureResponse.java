package com.kristalball.military.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenditureResponse(
        Long id,
        Long baseId,
        String baseName,
        Long assetId,
        String assetName,
        Integer quantity,
        LocalDate expenditureDate,
        String reason,
        String reference,
        String notes,
        String createdBy,
        LocalDateTime createdAt) {
}
