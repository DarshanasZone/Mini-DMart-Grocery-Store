package com.miniproject.minidmart.service;

import com.miniproject.minidmart.dto.ExchangeRequestDto;
import com.miniproject.minidmart.dto.ExchangeResponse;
import com.miniproject.minidmart.entity.ExchangeRequest;
import com.miniproject.minidmart.entity.Order;
import com.miniproject.minidmart.entity.Product;
import com.miniproject.minidmart.entity.User;
import com.miniproject.minidmart.enums.ExchangeStatus;
import com.miniproject.minidmart.enums.OrderStatus;
import com.miniproject.minidmart.exception.BadRequestException;
import com.miniproject.minidmart.exception.InsufficientStockException;
import com.miniproject.minidmart.exception.ResourceNotFoundException;
import com.miniproject.minidmart.exception.UnauthorizedException;
import com.miniproject.minidmart.repository.ExchangeRequestRepository;
import com.miniproject.minidmart.repository.OrderRepository;
import com.miniproject.minidmart.repository.ProductRepository;
import com.miniproject.minidmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public ExchangeResponse createExchangeRequest(Long userId, ExchangeRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only request exchanges for your own orders.");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Exchange can only be requested for orders in DELIVERED status. Current status: " + order.getStatus());
        }

        Product oldProduct = productRepository.findById(request.getOldProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Old product not found with ID: " + request.getOldProductId()));

        Product newProduct = productRepository.findById(request.getNewProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Replacement product not found with ID: " + request.getNewProductId()));

        if (newProduct.getStockQuantity() <= 0) {
            throw new InsufficientStockException("Replacement product '" + newProduct.getName() + "' is out of stock.");
        }

        ExchangeRequest exchangeRequest = ExchangeRequest.builder()
                .order(order)
                .user(user)
                .oldProduct(oldProduct)
                .newProduct(newProduct)
                .reason(request.getReason().trim())
                .status(ExchangeStatus.REQUESTED)
                .build();

        ExchangeRequest saved = exchangeRequestRepository.save(exchangeRequest);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getExchangesByUser(Long userId) {
        return exchangeRequestRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getUser().getId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getAllExchanges() {
        return exchangeRequestRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ExchangeResponse approveExchange(Long exchangeId, User adminUser) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found with ID: " + exchangeId));

        Product newProduct = exchangeRequest.getNewProduct();
        if (newProduct.getStockQuantity() <= 0) {
            throw new InsufficientStockException("Cannot approve exchange: Replacement stock is depleted for '" + newProduct.getName() + "'.");
        }

        // Deduct replacement unit & Restock returned old unit
        newProduct.setStockQuantity(newProduct.getStockQuantity() - 1);
        productRepository.save(newProduct);

        Product oldProduct = exchangeRequest.getOldProduct();
        oldProduct.setStockQuantity(oldProduct.getStockQuantity() + 1);
        productRepository.save(oldProduct);

        exchangeRequest.setStatus(ExchangeStatus.APPROVED);
        exchangeRequest.setProcessedAt(LocalDateTime.now());

        ExchangeRequest updated = exchangeRequestRepository.save(exchangeRequest);

        auditService.log(
                adminUser,
                "ADMIN_APPROVED_EXCHANGE",
                "ExchangeRequest",
                updated.getId(),
                "Admin approved exchange #" + updated.getId() + " (" + oldProduct.getName() + " ➔ " + newProduct.getName() + ")"
        );

        return mapToResponse(updated);
    }

    @Transactional
    public ExchangeResponse rejectExchange(Long exchangeId, User adminUser) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found with ID: " + exchangeId));

        exchangeRequest.setStatus(ExchangeStatus.REJECTED);
        exchangeRequest.setProcessedAt(LocalDateTime.now());

        ExchangeRequest updated = exchangeRequestRepository.save(exchangeRequest);

        auditService.log(
                adminUser,
                "ADMIN_REJECTED_EXCHANGE",
                "ExchangeRequest",
                updated.getId(),
                "Admin rejected exchange request #" + updated.getId()
        );

        return mapToResponse(updated);
    }

    private ExchangeResponse mapToResponse(ExchangeRequest e) {
        return ExchangeResponse.builder()
                .id(e.getId())
                .orderId(e.getOrder() != null ? e.getOrder().getId() : null)
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .userEmail(e.getUser() != null ? e.getUser().getEmail() : "N/A")
                .oldProductId(e.getOldProduct() != null ? e.getOldProduct().getId() : null)
                .oldProductName(e.getOldProduct() != null ? e.getOldProduct().getName() : "Item")
                .newProductId(e.getNewProduct() != null ? e.getNewProduct().getId() : null)
                .newProductName(e.getNewProduct() != null ? e.getNewProduct().getName() : "Item")
                .reason(e.getReason())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .processedAt(e.getProcessedAt())
                .build();
    }
}
