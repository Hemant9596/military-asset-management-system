package com.kristalball.military.controller;

import com.kristalball.military.dto.OpeningBalanceRequest;
import com.kristalball.military.dto.OpeningBalanceResponse;
import com.kristalball.military.service.BaseAccessService;
import com.kristalball.military.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opening-balances")
@PreAuthorize("hasRole('ADMIN')")
public class OpeningBalanceController {

    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;

    public OpeningBalanceController(InventoryService inventoryService, BaseAccessService baseAccessService) {
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping
    public List<OpeningBalanceResponse> getOpeningBalances() {
        baseAccessService.currentUser();
        return inventoryService.getOpeningBalances();
    }

    @PostMapping
    public ResponseEntity<OpeningBalanceResponse> createOpeningBalance(
            @Valid @RequestBody OpeningBalanceRequest request) {
        return ResponseEntity.ok(inventoryService.createOpeningBalance(request, baseAccessService.currentUser()));
    }
}