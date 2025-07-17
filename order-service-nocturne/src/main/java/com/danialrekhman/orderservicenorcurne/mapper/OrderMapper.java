package com.danialrekhman.orderservicenorcurne.mapper;

import com.danialrekhman.orderservicenorcurne.dto.OrderItemRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderItemResponseDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderResponseDTO;
import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userEmail(order.getUserEmail())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .items(order.getItems().stream()
                        .map(this::toDto)
                        .toList())
                .totalPrice(order.getTotalPrice())
                .build();
    }

    public OrderItemResponseDTO toDto(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .build();
    }

    public Order toEntity(OrderRequestDTO dto) {
        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.NEW)
                .build();

        List<OrderItem> items = dto.getItems().stream()
                .map(this::toEntity)
                .peek(item -> item.setOrder(order))
                .toList();

        order.setItems(items);
        return order;
    }

    public OrderItem toEntity(OrderItemRequestDTO dto) {
        return OrderItem.builder()
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .build();
    }
}

