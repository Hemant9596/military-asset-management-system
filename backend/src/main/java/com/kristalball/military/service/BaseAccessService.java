package com.kristalball.military.service;

import com.kristalball.military.entity.RoleName;
import com.kristalball.military.entity.User;
import com.kristalball.military.repository.BaseRepository;
import com.kristalball.military.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BaseAccessService {

    private final BaseRepository baseRepository;
    private final UserRepository userRepository;

    public BaseAccessService(BaseRepository baseRepository, UserRepository userRepository) {
        this.baseRepository = baseRepository;
        this.userRepository = userRepository;
    }

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public Long resolveBaseId(User user, Long requestedBaseId) {
        if (user.getRole() != null && user.getRole().getName() == RoleName.ADMIN) {
            return requestedBaseId;
        }
        Long assignedBaseId = user.getAssignedBase() == null ? null : user.getAssignedBase().getId();
        if (assignedBaseId == null) {
            throw new AccessDeniedException("User is not assigned to a base");
        }
        if (requestedBaseId != null && !Objects.equals(assignedBaseId, requestedBaseId)) {
            throw new AccessDeniedException("User is not authorized for the requested base");
        }
        return assignedBaseId;
    }

    public void enforceBaseAccess(User user, Long baseId) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (user.getRole() == null) {
            throw new AccessDeniedException("User role is required");
        }
        if (user.getRole().getName() == RoleName.ADMIN) {
            return;
        }

        if (baseId == null) {
            throw new AccessDeniedException("Base is required");
        }

        if (user.getAssignedBase() == null || !Objects.equals(user.getAssignedBase().getId(), baseId)) {
            throw new AccessDeniedException("User is not authorized for the requested base");
        }
    }

    public boolean canAccess(User user, Long baseId) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        if (user.getRole().getName() == RoleName.ADMIN) {
            return true;
        }
        return baseId != null && user.getAssignedBase() != null
                && Objects.equals(user.getAssignedBase().getId(), baseId);
    }

    public boolean canAccessTransfer(User user, Long sourceBaseId, Long destinationBaseId) {
        return user != null && (user.getRole() != null && user.getRole().getName() == RoleName.ADMIN
                || canAccess(user, sourceBaseId) || canAccess(user, destinationBaseId));
    }

    public void enforceTransferAccess(User user, Long sourceBaseId, Long destinationBaseId) {
        if (!canAccessTransfer(user, sourceBaseId, destinationBaseId)) {
            throw new AccessDeniedException("User is not authorized for this transfer");
        }
    }
}
