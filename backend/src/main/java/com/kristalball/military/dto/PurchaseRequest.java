package com.kristalball.military.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
        @NotNull(message = "Base is required") Long baseId,
        @NotNull(message = "Asset is required") Long assetId,
        @Positive(message = "Quantity must be greater than zero") Integer quantity,
        @NotNull(message = "Purchase date is required") LocalDate purchaseDate,
        String referenceNumber,
        String vendor,
        @NotNull(message = "Unit cost is required") BigDecimal unitCost,
        String notes) {
}
