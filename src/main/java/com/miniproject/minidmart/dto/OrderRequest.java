package com.miniproject.minidmart.dto;

import com.miniproject.minidmart.enums.DeliveryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "Delivery type is required")
    private DeliveryType deliveryType;

    private String deliveryAddress;

    private LocalDateTime pickupDate;
}
