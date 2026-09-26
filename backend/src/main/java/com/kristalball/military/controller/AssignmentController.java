package com.kristalball.military.controller;

import com.kristalball.military.dto.AssignmentRequest;
import com.kristalball.military.dto.AssignmentResponse;
import com.kristalball.military.entity.Assignment;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.AssignmentRepository;
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
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final BaseAccessService baseAccessService;

    public AssignmentController(AssignmentRepository assignmentRepository, UserRepository userRepository,
            InventoryService inventoryService, BaseAccessService baseAccessService) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER')")
    public List<AssignmentResponse> getAssignments() {
        var user = baseAccessService.currentUser();
        return assignmentRepository.findAll().stream()
                .filter(assignment -> baseAccessService.canAccess(user, assignment.getBase().getId()))
                .map(this::toResponse).toList();
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER')")
    public ResponseEntity<AssignmentResponse> createAssignment(@Valid @RequestBody AssignmentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(inventoryService.createAssignment(request, user));
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getBase().getId(),
                assignment.getBase().getName(),
                assignment.getAsset().getId(),
                assignment.getAsset().getName(),
                assignment.getAssignedTo().getId(),
                assignment.getAssignedTo().getFirstName() + " " + assignment.getAssignedTo().getLastName(),
                assignment.getQuantity(),
                assignment.getAssignmentDate(),
                assignment.getStatus(),
                assignment.getNotes(),
                assignment.getCreatedBy().getFirstName() + " " + assignment.getCreatedBy().getLastName(),
                assignment.getCreatedAt());
    }
}
