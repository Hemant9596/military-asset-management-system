package com.kristalball.military.controller;

import com.kristalball.military.dto.ExpenditureRequest;
import com.kristalball.military.dto.ExpenditureResponse;
import com.kristalball.military.entity.Expenditure;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.ExpenditureRepository;
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
public class ExpenditureController {

    private final ExpenditureRepository expenditureRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;

    public ExpenditureController(ExpenditureRepository expenditureRepository, UserRepository userRepository,
            InventoryService inventoryService, BaseAccessService baseAccessService) {
        this.expenditureRepository = expenditureRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/expenditures")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER')")
    public List<ExpenditureResponse> getExpenditures() {
        var user = baseAccessService.currentUser();
        return expenditureRepository.findAll().stream()
                .filter(expenditure -> baseAccessService.canAccess(user, expenditure.getBase().getId()))
                .map(this::toResponse).toList();
    }

    @PostMapping("/expenditures")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<ExpenditureResponse> createExpenditure(@Valid @RequestBody ExpenditureRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(inventoryService.createExpenditure(request, user));
    }

    private ExpenditureResponse toResponse(Expenditure expenditure) {
        return new ExpenditureResponse(
                expenditure.getId(),
                expenditure.getBase().getId(),
                expenditure.getBase().getName(),
                expenditure.getAsset().getId(),
                expenditure.getAsset().getName(),
                expenditure.getQuantity(),
                expenditure.getExpenditureDate(),
                expenditure.getReason(),
                expenditure.getReference(),
                expenditure.getNotes(),
                expenditure.getCreatedBy().getFirstName() + " " + expenditure.getCreatedBy().getLastName(),
                expenditure.getCreatedAt());
    }
}
