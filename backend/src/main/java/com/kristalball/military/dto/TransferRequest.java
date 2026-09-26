package com.kristalball.military.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TransferRequest(
        @NotNull(message = "Source base is required") Long sourceBaseId,
        @NotNull(message = "Destination base is required") Long destinationBaseId,
        @NotNull(message = "Asset is required") Long assetId,
        @Positive(message = "Quantity must be greater than zero") Integer quantity,
        @NotNull(message = "Transfer date is required") LocalDate transferDate,
        String referenceNumber,
        String notes) {
}
