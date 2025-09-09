package com.danialrekhman.orderservicenorcurne.dto;

import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponseDTO {
    Long id;
    String userEmail;
    LocalDateTime orderDate;
    OrderStatus status;
    List<OrderItemResponseDTO> items;
    BigDecimal totalPrice;
}

