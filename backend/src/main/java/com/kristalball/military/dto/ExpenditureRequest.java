package com.kristalball.military.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ExpenditureRequest(
        @NotNull(message = "Base is required") Long baseId,
        @NotNull(message = "Asset is required") Long assetId,
        @Positive(message = "Quantity must be greater than zero") Integer quantity,
        @NotNull(message = "Date is required") LocalDate expenditureDate,
        @NotBlank(message = "Reason is required") String reason,
        String reference,
        String notes) {
}
