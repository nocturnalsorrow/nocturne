package com.danialrekhman.commonevents;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCheckMessage {
    private String correlationId;
    private Long productId;
    private int quantity;
    private BigDecimal priceAtOrder;
    private Boolean available;
    private String message;
}
