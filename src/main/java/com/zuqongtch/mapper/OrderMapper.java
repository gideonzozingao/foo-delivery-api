
package com.zuqongtch.mapper;

import com.zuqongtch.dto.order.OrderResponse;
import com.zuqongtch.entity.Order;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemDto.builder()
                                .menuItemId(item.getMenuItem().getId())
                                .menuItemName(item.getMenuItem().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPriceAtOrder())
                                .specialInstructions(item.getSpecialInstructions())
                                .build())
                        .collect(Collectors.toList()))
                .deliveryAddress(OrderResponse.AddressDto.builder()
                        .addressLine1(order.getDeliveryAddress().getAddressLine1())
                        .addressLine2(order.getDeliveryAddress().getAddressLine2())
                        .city(order.getDeliveryAddress().getCity())
                        .state(order.getDeliveryAddress().getState())
                        .postalCode(order.getDeliveryAddress().getPostalCode())
                        .phoneNumber(order.getDeliveryAddress().getPhoneNumber())
                        .build())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .tax(order.getTax())
                .total(order.getTotal())
                .status(order.getStatus())
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus() : null)
                .paymentMethod(order.getPayment() != null ? order.getPayment().getPaymentMethod() : null)
                .specialInstructions(order.getSpecialInstructions())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .createdAt(order.getCreatedAt())
                .build();
    }
}