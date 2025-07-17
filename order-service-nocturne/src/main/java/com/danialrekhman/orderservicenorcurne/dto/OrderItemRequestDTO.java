package com.danialrekhman.orderservicenorcurne.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderItemRequestDTO {
    Long productId;
    int quantity;
    Long orderId;
}
