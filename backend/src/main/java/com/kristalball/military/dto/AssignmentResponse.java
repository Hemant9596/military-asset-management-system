package com.kristalball.military.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long baseId,
        String baseName,
        Long assetId,
        String assetName,
        Long assignedToUserId,
        String assignedToName,
        Integer quantity,
        LocalDate assignmentDate,
        String status,
        String notes,
        String createdBy,
        LocalDateTime createdAt) {
}
