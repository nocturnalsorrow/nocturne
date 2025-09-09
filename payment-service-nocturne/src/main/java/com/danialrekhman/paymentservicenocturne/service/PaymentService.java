package com.danialrekhman.paymentservicenocturne.service;

import com.danialrekhman.commonevents.OrderCreatedEvent;
import com.danialrekhman.paymentservicenocturne.dto.PaymentRequestDTO;
import com.danialrekhman.paymentservicenocturne.dto.PaymentResponseDTO;
import com.danialrekhman.paymentservicenocturne.model.Payment;
import com.danialrekhman.paymentservicenocturne.model.PaymentStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentService {

    void createAndProcessPayment(OrderCreatedEvent event);

    Payment getPaymentEntityById(Long id, Authentication authentication);

    PaymentResponseDTO getPaymentById(Long id, Authentication authentication);

    List<Payment> getPaymentsByOrderId(Long orderId, Authentication authentication);

    List<Payment> getAllPayments(Authentication authentication);

    void updatePaymentStatus(Long paymentId, PaymentStatus newStatus, Authentication authentication);
}

