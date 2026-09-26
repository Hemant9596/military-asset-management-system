package com.kristalball.military.service;

import com.kristalball.military.entity.AuditLog;
import com.kristalball.military.entity.User;
import com.kristalball.military.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User user, String action, String entityType, Long entityId, String description,
            String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
