package com.kristalball.military.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record OpeningBalanceRequest(
        @NotNull Long baseId,
        @NotNull Long assetId,
        @Positive Integer quantity,
        @NotNull LocalDate effectiveDate) {
}