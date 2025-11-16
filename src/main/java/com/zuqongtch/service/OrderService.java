package com.zuqongtch.service;

import com.zuqongtch.constant.OrderStatus;
import com.zuqongtch.dto.order.OrderRequest;
import com.zuqongtch.dto.order.OrderResponse;
import com.zuqongtch.entity.*;
import com.zuqongtch.exception.ResourceNotFoundException;
import com.zuqongtch.mapper.OrderMapper;
import com.zuqongtch.repository.*;
import com.zuqongtch.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final SecurityUtils securityUtils;
    private final OrderMapper orderMapper;

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("5.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Get user's cart
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalStateException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order with empty cart");
        }

        // Calculate totals
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE);
        BigDecimal total = subtotal.add(DELIVERY_FEE).add(tax);

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(currentUser)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .deliveryFee(DELIVERY_FEE)
                .tax(tax)
                .total(total)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .specialInstructions(request.getSpecialInstructions())
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(45))
                .street(request.getStreet())
                .apartmentNumber(request.getApartmentNumber())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .items(new ArrayList<>())
                .build();

        order = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(cartItem.getMenuItem())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .specialInstructions(cartItem.getSpecialInstructions())
                    .build();
            orderItemRepository.save(orderItem);
            order.getItems().add(orderItem);
        }

        // Clear cart
        cartItemRepository.deleteAll(cart.getItems());

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User currentUser = securityUtils.getCurrentUser();
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(currentUser);
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Verify order belongs to current user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to view this order");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        User currentUser = securityUtils.getCurrentUser();
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

        // Verify order belongs to current user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to view this order");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Verify order belongs to current user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to cancel this order");
        }

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    // Admin methods
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(status);

        if (status == OrderStatus.DELIVERED) {
            order.setPaymentStatus("COMPLETED");
        }

        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}