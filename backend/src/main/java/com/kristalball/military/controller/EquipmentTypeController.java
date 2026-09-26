package com.kristalball.military.controller;

import com.kristalball.military.dto.EquipmentTypeResponse;
import com.kristalball.military.dto.EquipmentTypeRequest;
import com.kristalball.military.entity.EquipmentType;
import com.kristalball.military.repository.EquipmentTypeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EquipmentTypeController {

    private final EquipmentTypeRepository equipmentTypeRepository;

    public EquipmentTypeController(EquipmentTypeRepository equipmentTypeRepository) {
        this.equipmentTypeRepository = equipmentTypeRepository;
    }

    @GetMapping("/equipment-types")
    public List<EquipmentTypeResponse> getEquipmentTypes() {
        return equipmentTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping("/equipment-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentTypeResponse> createEquipmentType(
            @Valid @RequestBody EquipmentTypeRequest request) {
        EquipmentType type = EquipmentType.builder().name(request.name().trim())
                .description(request.description()).build();
        return ResponseEntity.ok(toResponse(equipmentTypeRepository.save(type)));
    }

    private EquipmentTypeResponse toResponse(EquipmentType equipmentType) {
        return new EquipmentTypeResponse(equipmentType.getId(), equipmentType.getName(),
                equipmentType.getDescription());
    }
}
