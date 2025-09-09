package com.danialrekhman.orderservicenorcurne.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponseDTO {
    Long id;
    Long productId;
    int quantity;
    BigDecimal priceAtOrder;
}
