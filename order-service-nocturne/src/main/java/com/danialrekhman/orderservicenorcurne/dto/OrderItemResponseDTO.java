package com.danialrekhman.orderservicenorcurne.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderItemResponseDTO {
    Long id;
    Long productId;
    int quantity;
    BigDecimal priceAtOrder;
}
