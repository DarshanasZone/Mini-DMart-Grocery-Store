package com.miniproject.minidmart.service;

import com.miniproject.minidmart.dto.AuditLogResponse;
import com.miniproject.minidmart.entity.AuditLog;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entityName, Long entityId, String details) {
        try {
            AuditLog logEntry = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.error("Failed to persist audit log: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .map(l -> AuditLogResponse.builder()
                        .id(l.getId())
                        .userEmail(l.getUser() != null ? l.getUser().getEmail() : "SYSTEM")
                        .action(l.getAction())
                        .entityName(l.getEntityName())
                        .entityId(l.getEntityId())
                        .details(l.getDetails())
                        .timestamp(l.getTimestamp())
                        .build())
                .toList();
    }
}
