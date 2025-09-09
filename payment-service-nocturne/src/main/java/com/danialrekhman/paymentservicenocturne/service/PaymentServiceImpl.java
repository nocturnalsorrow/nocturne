package com.danialrekhman.paymentservicenocturne.service;

import com.danialrekhman.commonevents.OrderCreatedEvent;
import com.danialrekhman.commonevents.PaymentProcessedEvent;
import com.danialrekhman.paymentservicenocturne.dto.PaymentResponseDTO;
import com.danialrekhman.paymentservicenocturne.exception.InvalidPaymentDataException;
import com.danialrekhman.paymentservicenocturne.exception.PaymentNotFoundException;
import com.danialrekhman.paymentservicenocturne.exception.PaymentProcessingException;
import com.danialrekhman.paymentservicenocturne.kafka.PaymentEventProducer;
import com.danialrekhman.paymentservicenocturne.mapper.PaymentMapper;
import com.danialrekhman.paymentservicenocturne.model.Payment;
import com.danialrekhman.paymentservicenocturne.model.PaymentMethod;
import com.danialrekhman.paymentservicenocturne.model.PaymentStatus;
import com.danialrekhman.paymentservicenocturne.repository.PaymentRepository;
import com.danialrekhman.paymentservicenocturne.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public void createAndProcessPayment(OrderCreatedEvent event) {
        // тут Authentication не нужен, т.к. платеж создается асинхронно по событию из OrderService
        try {
            Payment payment = Payment.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .userEmail(event.getUserEmail())
                    .amount(event.getTotalPrice())
                    .method(PaymentMethod.MOCK)
                    .status(PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            payment = paymentRepository.save(payment);

            PaymentStatus result = processPaymentMock(payment);
            payment.setStatus(result);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            PaymentProcessedEvent response = PaymentProcessedEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .transactionId(payment.getTransactionId())
                    .amount(payment.getAmount())
                    .status(payment.getStatus().name())
                    .method(payment.getMethod().name())
                    .build();

            eventProducer.publishPaymentProcessed(response);

        } catch (Exception e) {
            log.error("Payment processing failed for orderId={}", event.getOrderId(), e);
            throw new PaymentProcessingException("Failed to process payment for orderId " + event.getOrderId());
        }
    }

    private PaymentStatus processPaymentMock(Payment payment) {
        if (payment.getMethod() == PaymentMethod.MOCK) {
            return PaymentStatus.SUCCESS;
        }
        return new Random().nextInt(10) < 8 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentEntityById(Long id, Authentication authentication) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));

        if (!isAdmin(authentication) && !payment.getUserEmail().equals(authentication.getName())) {
            throw new CustomAccessDeniedException("You don't have access to this payment.");
        }
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id, Authentication authentication) {
        return paymentMapper.toDto(getPaymentEntityById(id, authentication));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderId(Long orderId, Authentication authentication) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException("No payments found for order ID " + orderId);
        }

        if (!isAdmin(authentication)) {
            String userEmail = authentication.getName();
            boolean ownsOrder = payments.stream().anyMatch(p -> p.getUserEmail().equals(userEmail));
            if (!ownsOrder) {
                throw new CustomAccessDeniedException("You don't have access to these payments.");
            }
        }

        return payments;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new CustomAccessDeniedException("Only admin can view all payments.");
        }
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus newStatus, Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new CustomAccessDeniedException("Only admin can update payment status.");
        }

        Payment p = getPaymentEntityById(paymentId, authentication);
        if (newStatus == null) {
            throw new InvalidPaymentDataException("Payment status cannot be null");
        }

        p.setStatus(newStatus);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
