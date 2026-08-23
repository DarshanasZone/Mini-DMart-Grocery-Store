package com.miniproject.minidmart.dto;

import com.miniproject.minidmart.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private String reason;
    private ReturnStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
