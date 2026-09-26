package com.kristalball.military.controller;

import com.kristalball.military.dto.InventoryResponse;
import com.kristalball.military.service.InventoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory")
    @PreAuthorize("isAuthenticated()")
    public List<InventoryResponse> getInventory(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId) {
        return inventoryService.getInventory(baseId, equipmentTypeId);
    }
}
