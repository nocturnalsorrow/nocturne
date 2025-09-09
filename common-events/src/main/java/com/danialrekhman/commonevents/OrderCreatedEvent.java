package com.danialrekhman.commonevents;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    Long orderId;
    String userEmail;
    BigDecimal totalPrice;
}
