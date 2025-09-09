package com.danialrekhman.commonevents;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent {
    Long orderId;
    Long paymentId;
    String transactionId;
    BigDecimal amount;
    String status; // SUCCESS / FAILED
    String method; // MOCK / CARD / etc.
}
