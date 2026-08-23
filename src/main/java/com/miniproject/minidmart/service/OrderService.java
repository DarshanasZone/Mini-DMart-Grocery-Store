package com.miniproject.minidmart.service;

import com.miniproject.minidmart.dto.OrderItemResponse;
import com.miniproject.minidmart.dto.OrderRequest;
import com.miniproject.minidmart.dto.OrderResponse;
import com.miniproject.minidmart.entity.*;
import com.miniproject.minidmart.enums.OrderStatus;
import com.miniproject.minidmart.exception.BadRequestException;
import com.miniproject.minidmart.exception.InsufficientStockException;
import com.miniproject.minidmart.exception.ResourceNotFoundException;
import com.miniproject.minidmart.exception.UnauthorizedException;
import com.miniproject.minidmart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart not found for user."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty shopping cart.");
        }

        // Validate stock and product active status
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getActive() == null || !product.getActive()) {
                throw new BadRequestException("Product '" + product.getName() + "' is inactive and unavailable.");
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for '" + product.getName() + "'. Available: " +
                        product.getStockQuantity() + ", requested: " + item.getQuantity()
                );
            }
        }

        // Calculate total amount on backend
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        // Create Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PLACED)
                .deliveryType(request.getDeliveryType())
                .deliveryAddress(request.getDeliveryAddress())
                .pickupDate(request.getPickupDate())
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create OrderItems & deduct stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            savedOrder.getItems().add(orderItem);
            orderItemRepository.save(orderItem);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // Audit Log
        auditService.log(
                user,
                "ORDER_PLACED",
                "Order",
                savedOrder.getId(),
                "Customer placed order #" + savedOrder.getId() + " totaling ₹" + savedOrder.getTotalAmount()
        );

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser() != null && o.getUser().getId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!isAdmin && (order.getUser() == null || !order.getUser().getId().equals(userId))) {
            throw new UnauthorizedException("You do not have permission to view this order.");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getStatus());
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        auditService.log(
                order.getUser(),
                "ORDER_CANCELLED",
                "Order",
                updatedOrder.getId(),
                "Customer cancelled order #" + updatedOrder.getId()
        );

        return mapToResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, User adminUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        // If admin transitions to CANCELLED, restore product stock
        if (newStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        auditService.log(
                adminUser,
                "ADMIN_UPDATED_ORDER_STATUS",
                "Order",
                updated.getId(),
                "Admin updated order #" + updated.getId() + " status from " + currentStatus + " to " + newStatus
        );

        return mapToResponse(updated);
    }

    public OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                itemResponses.add(OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProduct() != null ? item.getProduct().getName() : "Item")
                        .productImageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build());
            }
        }

        boolean canCancel = order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.CONFIRMED;
        boolean canReturnOrExchange = order.getStatus() == OrderStatus.DELIVERED;

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userName(order.getUser() != null ? order.getUser().getName() : "Customer")
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : "N/A")
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryType(order.getDeliveryType())
                .deliveryAddress(order.getDeliveryAddress())
                .pickupDate(order.getPickupDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .canCancel(canCancel)
                .canReturnOrExchange(canReturnOrExchange)
                .build();
    }
}
