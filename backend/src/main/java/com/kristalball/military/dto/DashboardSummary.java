package com.kristalball.military.dto;

import java.util.List;
import java.util.Map;

public record DashboardSummary(
        Integer openingBalance,
        Integer purchases,
        Integer transferIn,
        Integer transferOut,
        Integer netMovement,
        Integer assignedAssets,
        Integer expendedAssets,
        Integer closingBalance,
        List<Map<String, Object>> movementTrend,
        List<Map<String, Object>> inventoryByType,
        List<Map<String, Object>> transfersByBase) {
}
