package com.kristalball.military.service;

import com.kristalball.military.dto.AuthRequest;
import com.kristalball.military.dto.AuthResponse;
import com.kristalball.military.dto.UserResponse;
import com.kristalball.military.entity.User;
import com.kristalball.military.exception.BusinessException;
import com.kristalball.military.repository.UserRepository;
import com.kristalball.military.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
            JwtService jwtService, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        auditService.logAction(user, "LOGIN", "USER", user.getId(), "Successful user login", null);

        return new AuthResponse(token, toUserResponse(user));
    }

    public UserResponse getCurrentUser(User user) {
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
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
