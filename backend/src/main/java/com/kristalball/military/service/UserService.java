package com.kristalball.military.service;

import com.kristalball.military.dto.UserResponse;
import com.kristalball.military.entity.Base;
import com.kristalball.military.entity.Role;
import com.kristalball.military.entity.RoleName;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.BusinessException;
import com.kristalball.military.exception.ResourceNotFoundException;
import com.kristalball.military.repository.BaseRepository;
import com.kristalball.military.repository.RoleRepository;
import com.kristalball.military.repository.UserRepository;
import com.kristalball.military.service.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BaseRepository baseRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BaseRepository baseRepository,
            PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.baseRepository = baseRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse createUser(String firstName, String lastName, String email, String password, RoleName roleName,
            Long baseId, User createdBy) {
        email = email.trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("User with this email already exists");
        }
        if (roleName != RoleName.ADMIN && baseId == null) {
            throw new BusinessException("A base is required for non-admin users");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        Base base = null;
        if (baseId != null) {
            base = baseRepository.findById(baseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Base not found"));
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .assignedBase(base)
                .enabled(true)
                .build();

        userRepository.save(user);
        auditService.logAction(createdBy, "CREATE_USER", "USER", user.getId(),
                "Created " + roleName + " account for " + email, null);
        return toResponse(user);
    }

    public List<UserResponse> getAssignableUsers(Long baseId) {
        return userRepository.findAll().stream()
                .filter(user -> baseId == null || user.getAssignedBase() != null
                        && user.getAssignedBase().getId().equals(baseId))
                .filter(User::isEnabled)
                .map(this::toResponse)
                .toList();
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName().name() : null,
                user.getAssignedBase() != null ? user.getAssignedBase().getId() : null,
                user.getAssignedBase() != null ? user.getAssignedBase().getName() : null,
                user.isEnabled());
    }
}
