package com.miniproject.minidmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String userEmail;
    private String action;
    private String entityName;
    private Long entityId;
    private String details;
    private LocalDateTime timestamp;
}
