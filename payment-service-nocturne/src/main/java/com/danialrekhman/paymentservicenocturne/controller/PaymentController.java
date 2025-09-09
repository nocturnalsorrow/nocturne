package com.danialrekhman.paymentservicenocturne.controller;

import com.danialrekhman.paymentservicenocturne.dto.PaymentResponseDTO;
import com.danialrekhman.paymentservicenocturne.mapper.PaymentMapper;
import com.danialrekhman.paymentservicenocturne.model.PaymentStatus;
import com.danialrekhman.paymentservicenocturne.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(paymentService.getPaymentById(id, authentication));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByOrderId(@PathVariable Long orderId, Authentication authentication) {
        List<PaymentResponseDTO> list = paymentService.getPaymentsByOrderId(orderId, authentication)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments(Authentication authentication) {
        List<PaymentResponseDTO> list = paymentService.getAllPayments(authentication)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody PaymentStatus newStatus,
            Authentication authentication) {
        paymentService.updatePaymentStatus(paymentId, newStatus, authentication);
        return ResponseEntity.noContent().build();
    }
}



