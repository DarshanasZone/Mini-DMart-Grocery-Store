package com.miniproject.minidmart.dto;

import com.miniproject.minidmart.enums.DeliveryType;
import com.miniproject.minidmart.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private OrderStatus status;
    private DeliveryType deliveryType;
    private String deliveryAddress;
    private LocalDateTime pickupDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canCancel;
    private boolean canReturnOrExchange;
}
