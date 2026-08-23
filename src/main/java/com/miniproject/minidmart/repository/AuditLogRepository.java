package com.miniproject.minidmart.repository;

import com.miniproject.minidmart.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}