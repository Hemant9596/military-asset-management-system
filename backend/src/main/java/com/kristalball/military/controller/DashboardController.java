package com.kristalball.military.controller;

import com.kristalball.military.dto.DashboardSummary;
import com.kristalball.military.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.kristalball.military.service.BaseAccessService;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final BaseAccessService baseAccessService;

    public DashboardController(DashboardService dashboardService, BaseAccessService baseAccessService) {
        this.dashboardService = dashboardService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public DashboardSummary getDashboard(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        baseAccessService.currentUser();
        return dashboardService.getDashboard(baseId, equipmentTypeId, from, to);
    }

    @GetMapping("/dashboard/movement-details")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getMovementDetails(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return dashboardService.getMovementDetails(baseId, equipmentTypeId, from, to);
    }
}
