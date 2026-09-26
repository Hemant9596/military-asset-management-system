package com.kristalball.military.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        Long sourceBaseId,
        String sourceBaseName,
        Long destinationBaseId,
        String destinationBaseName,
        Long assetId,
        String assetName,
        Integer quantity,
        LocalDate transferDate,
        String referenceNumber,
        String notes,
        String createdBy,
        LocalDateTime createdAt) {
}
