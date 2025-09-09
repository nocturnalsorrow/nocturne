package com.danialrekhman.paymentservicenocturne.dto;

import com.danialrekhman.paymentservicenocturne.model.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    Long id;
    Long orderId;
    BigDecimal amount;
    String status; // SUCCESS, FAILED, PENDING
    String transactionId;
}