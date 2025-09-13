package com.danialrekhman.commonevents;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private Long orderId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private String status; // SUCCESS / FAILED
    private String method; // MOCK / CARD / etc.
    private String userEmail;
}
