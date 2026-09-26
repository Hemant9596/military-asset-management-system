package com.kristalball.military.controller;

import com.kristalball.military.dto.BaseRequest;
import com.kristalball.military.dto.BaseResponse;
import com.kristalball.military.entity.Base;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.BaseRepository;
import com.kristalball.military.service.BaseAccessService;
import com.kristalball.military.entity.RoleName;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BaseController {

    private final BaseRepository baseRepository;
    private final BaseAccessService baseAccessService;

    public BaseController(BaseRepository baseRepository, BaseAccessService baseAccessService) {
        this.baseRepository = baseRepository;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/bases")
    public List<BaseResponse> getBases() {
        var user = baseAccessService.currentUser();
        return baseRepository.findAll().stream()
                .filter(base -> user.getRole().getName() != RoleName.BASE_COMMANDER
                        || baseAccessService.canAccess(user, base.getId()))
                .map(this::toResponse).toList();
    }

    @GetMapping("/bases/{id}")
    public BaseResponse getBase(@PathVariable Long id) {
        Base base = baseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Base not found"));
        var user = baseAccessService.currentUser();
        if (user.getRole().getName() == RoleName.BASE_COMMANDER) {
            baseAccessService.enforceBaseAccess(user, id);
        }
        return toResponse(base);
    }

    @PostMapping("/bases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> createBase(@Valid @RequestBody BaseRequest request) {
        Base base = Base.builder()
                .name(request.name())
                .location(request.location())
                .description(request.description())
                .active(request.active())
                .build();
        Base saved = baseRepository.save(base);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/bases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> updateBase(@PathVariable Long id, @Valid @RequestBody BaseRequest request) {
        Base base = baseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Base not found"));
        base.setName(request.name());
        base.setLocation(request.location());
        base.setDescription(request.description());
        base.setActive(request.active());
        return ResponseEntity.ok(toResponse(baseRepository.save(base)));
    }

    @DeleteMapping("/bases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBase(@PathVariable Long id) {
        Base base = baseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Base not found"));
        baseRepository.delete(base);
        return ResponseEntity.noContent().build();
    }

    private BaseResponse toResponse(Base base) {
        return new BaseResponse(base.getId(), base.getName(), base.getLocation(), base.getDescription(),
                base.isActive());
    }
}
