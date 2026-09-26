package com.kristalball.military.controller;

import com.kristalball.military.dto.TransferRequest;
import com.kristalball.military.dto.TransferResponse;
import com.kristalball.military.entity.Transfer;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.TransferRepository;
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
public class TransferController {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;

    public TransferController(TransferRepository transferRepository, UserRepository userRepository,
            InventoryService inventoryService, BaseAccessService baseAccessService) {
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public List<TransferResponse> getTransfers() {
        var user = baseAccessService.currentUser();
        return transferRepository.findAll().stream()
                .filter(transfer -> baseAccessService.canAccessTransfer(user, transfer.getSourceBase().getId(),
                        transfer.getDestinationBase().getId()))
                .map(this::toResponse).toList();
    }

    @GetMapping("/transfers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'BASE_COMMANDER')")
    public TransferResponse getTransfer(@PathVariable Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        baseAccessService.enforceTransferAccess(baseAccessService.currentUser(), transfer.getSourceBase().getId(),
                transfer.getDestinationBase().getId());
        return toResponse(transfer);
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(inventoryService.createTransfer(request, user));
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceBase().getId(),
                transfer.getSourceBase().getName(),
                transfer.getDestinationBase().getId(),
                transfer.getDestinationBase().getName(),
                transfer.getAsset().getId(),
                transfer.getAsset().getName(),
                transfer.getQuantity(),
                transfer.getTransferDate(),
                transfer.getReferenceNumber(),
                transfer.getNotes(),
                transfer.getCreatedBy().getFirstName() + " " + transfer.getCreatedBy().getLastName(),
                transfer.getCreatedAt());
    }
}
