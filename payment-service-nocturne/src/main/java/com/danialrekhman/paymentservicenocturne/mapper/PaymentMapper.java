package com.danialrekhman.paymentservicenocturne.mapper;

import com.danialrekhman.paymentservicenocturne.dto.PaymentRequestDTO;
import com.danialrekhman.paymentservicenocturne.dto.PaymentResponseDTO;
import com.danialrekhman.paymentservicenocturne.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDTO dto) {
        return Payment.builder()
                .orderId(dto.getOrderId())
                .build();
    }

    public PaymentResponseDTO toDto(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .build();
    }
}

