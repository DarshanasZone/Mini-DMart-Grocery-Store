package com.miniproject.minidmart.controller;

import com.miniproject.minidmart.dto.AuditLogResponse;
import com.miniproject.minidmart.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin - Audit Logs", description = "Admin endpoints for viewing system audit history")
public class AdminAuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get all audit logs", description = "Retrieves chronological audit trail of all administrative actions")
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }
}
