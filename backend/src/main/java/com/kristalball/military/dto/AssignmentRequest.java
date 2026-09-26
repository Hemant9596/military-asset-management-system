package com.kristalball.military.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AssignmentRequest(
        @NotNull(message = "Base is required") Long baseId,
        @NotNull(message = "Asset is required") Long assetId,
        @NotNull(message = "Recipient user is required") Long assignedToUserId,
        @Positive(message = "Quantity must be greater than zero") Integer quantity,
        @NotNull(message = "Assignment date is required") LocalDate assignmentDate,
        @NotBlank(message = "Status is required") String status,
        String notes) {
}
