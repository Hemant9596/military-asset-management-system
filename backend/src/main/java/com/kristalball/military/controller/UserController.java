package com.kristalball.military.controller;

import com.kristalball.military.dto.UserResponse;
import com.kristalball.military.dto.UserRequest;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.UserRepository;
import com.kristalball.military.service.BaseAccessService;
import com.kristalball.military.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final BaseAccessService baseAccessService;

    public UserController(UserService userService, UserRepository userRepository, BaseAccessService baseAccessService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.baseAccessService = baseAccessService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {
        User createdBy = userRepository.findByEmail(baseAccessService.currentUser().getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userService.createUser(request.firstName(), request.lastName(), request.email(),
                request.password(), request.role(), request.baseId(), createdBy));
    }

    @GetMapping("/users/assignable")
    @PreAuthorize("hasAnyRole('ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER')")
    public List<UserResponse> getAssignableUsers(@RequestParam(required = false) Long baseId) {
        User currentUser = baseAccessService.currentUser();
        Long scopedBaseId = baseAccessService.resolveBaseId(currentUser, baseId);
        return userService.getAssignableUsers(scopedBaseId);
    }
}
