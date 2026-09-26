package com.kristalball.military.controller;

import com.kristalball.military.dto.AssetRequest;
import com.kristalball.military.dto.AssetResponse;
import com.kristalball.military.entity.Asset;
import com.kristalball.military.entity.EquipmentType;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.AssetRepository;
import com.kristalball.military.repository.EquipmentTypeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssetController {

    private final AssetRepository assetRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;

    public AssetController(AssetRepository assetRepository, EquipmentTypeRepository equipmentTypeRepository) {
        this.assetRepository = assetRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
    }

    @GetMapping("/assets")
    public List<AssetResponse> getAssets() {
        return assetRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping("/assets")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody AssetRequest request) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(request.equipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));
        Asset asset = Asset.builder()
                .name(request.name())
                .equipmentType(equipmentType)
                .serialNumber(request.serialNumber())
                .model(request.model())
                .unit(request.unit())
                .description(request.description())
                .active(request.active())
                .build();
        return ResponseEntity.ok(toResponse(assetRepository.save(asset)));
    }

    @PutMapping("/assets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<AssetResponse> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        Asset asset = assetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        EquipmentType equipmentType = equipmentTypeRepository.findById(request.equipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type not found"));
        asset.setName(request.name());
        asset.setEquipmentType(equipmentType);
        asset.setSerialNumber(request.serialNumber());
        asset.setModel(request.model());
        asset.setUnit(request.unit());
        asset.setDescription(request.description());
        asset.setActive(request.active());
        return ResponseEntity.ok(toResponse(assetRepository.save(asset)));
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getEquipmentType().getId(),
                asset.getEquipmentType().getName(),
                asset.getSerialNumber(),
                asset.getModel(),
                asset.getUnit(),
                asset.getDescription(),
                asset.isActive());
    }
}
