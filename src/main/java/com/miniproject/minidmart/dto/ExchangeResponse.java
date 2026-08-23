package com.miniproject.minidmart.dto;

import com.miniproject.minidmart.enums.ExchangeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private Long oldProductId;
    private String oldProductName;
    private Long newProductId;
    private String newProductName;
    private String reason;
    private ExchangeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
