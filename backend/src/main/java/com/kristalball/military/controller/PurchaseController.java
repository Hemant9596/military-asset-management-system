package com.kristalball.military.controller;

import com.kristalball.military.dto.PurchaseRequest;
import com.kristalball.military.dto.PurchaseResponse;
import com.kristalball.military.entity.Purchase;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.PurchaseRepository;
import com.kristalball.military.repository.UserRepository;
import com.kristalball.military.service.InventoryService;
import com.kristalball.military.service.BaseAccessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;

    public PurchaseController(PurchaseRepository purchaseRepository, UserRepository userRepository,
            InventoryService inventoryService, BaseAccessService baseAccessService) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public List<PurchaseResponse> getPurchases() {
        var user = baseAccessService.currentUser();
        return purchaseRepository.findAll().stream()
                .filter(purchase -> baseAccessService.canAccess(user, purchase.getBase().getId()))
                .map(this::toResponse).toList();
    }

    @GetMapping("/purchases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public PurchaseResponse getPurchase(@PathVariable Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));
        baseAccessService.enforceBaseAccess(baseAccessService.currentUser(), purchase.getBase().getId());
        return toResponse(purchase);
    }

    @PostMapping("/purchases")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(inventoryService.createPurchase(request, user));
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getBase().getId(),
                purchase.getBase().getName(),
                purchase.getAsset().getId(),
                purchase.getAsset().getName(),
                purchase.getQuantity(),
                purchase.getPurchaseDate(),
                purchase.getReferenceNumber(),
                purchase.getVendor(),
                purchase.getUnitCost(),
                purchase.getTotalCost(),
                purchase.getNotes(),
                purchase.getCreatedBy().getFirstName() + " " + purchase.getCreatedBy().getLastName(),
                purchase.getCreatedAt());
    }
}
