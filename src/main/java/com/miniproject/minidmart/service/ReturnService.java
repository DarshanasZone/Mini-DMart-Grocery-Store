package com.miniproject.minidmart.service;

import com.miniproject.minidmart.dto.ReturnRequestDto;
import com.miniproject.minidmart.dto.ReturnResponse;
import com.miniproject.minidmart.entity.Order;
import com.miniproject.minidmart.entity.OrderItem;
import com.miniproject.minidmart.entity.Product;
import com.miniproject.minidmart.entity.ReturnRequest;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.enums.OrderStatus;
import com.miniproject.minidmart.enums.ReturnStatus;
import com.miniproject.minidmart.exception.BadRequestException;
import com.miniproject.minidmart.exception.ResourceNotFoundException;
import com.miniproject.minidmart.exception.UnauthorizedException;
import com.miniproject.minidmart.repository.OrderRepository;
import com.miniproject.minidmart.repository.ProductRepository;
import com.miniproject.minidmart.repository.ReturnRequestRepository;
import com.miniproject.minidmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public ReturnResponse createReturnRequest(Long userId, ReturnRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only request returns for your own orders.");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Return can only be requested for orders in DELIVERED status. Current status: " + order.getStatus());
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .order(order)
                .user(user)
                .reason(request.getReason().trim())
                .status(ReturnStatus.REQUESTED)
                .build();

        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnsByUser(Long userId) {
        return returnRequestRepository.findAll().stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getAllReturns() {
        return returnRequestRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ReturnResponse approveReturn(Long returnId, User adminUser) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + returnId));

        returnRequest.setStatus(ReturnStatus.APPROVED);
        returnRequest.setProcessedAt(LocalDateTime.now());

        // Restock returned items
        if (returnRequest.getOrder() != null && returnRequest.getOrder().getItems() != null) {
            for (OrderItem item : returnRequest.getOrder().getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        ReturnRequest updated = returnRequestRepository.save(returnRequest);

        auditService.log(
                adminUser,
                "ADMIN_APPROVED_RETURN",
                "ReturnRequest",
                updated.getId(),
                "Admin approved return request #" + updated.getId() + " and restocked inventory."
        );

        return mapToResponse(updated);
    }

    @Transactional
    public ReturnResponse rejectReturn(Long returnId, User adminUser) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found with ID: " + returnId));

        returnRequest.setStatus(ReturnStatus.REJECTED);
        returnRequest.setProcessedAt(LocalDateTime.now());

        ReturnRequest updated = returnRequestRepository.save(returnRequest);

        auditService.log(
                adminUser,
                "ADMIN_REJECTED_RETURN",
                "ReturnRequest",
                updated.getId(),
                "Admin rejected return request #" + updated.getId()
        );

        return mapToResponse(updated);
    }

    private ReturnResponse mapToResponse(ReturnRequest r) {
        return ReturnResponse.builder()
                .id(r.getId())
                .orderId(r.getOrder() != null ? r.getOrder().getId() : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userEmail(r.getUser() != null ? r.getUser().getEmail() : "N/A")
                .reason(r.getReason())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .processedAt(r.getProcessedAt())
                .build();
    }
}
