package com.danialrekhman.orderservicenorcurne.dto;

import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    Long id;
    String userEmail;
    LocalDateTime orderDate;
    OrderStatus status;
    List<OrderItemResponseDTO> items;
    BigDecimal totalPrice;
}

