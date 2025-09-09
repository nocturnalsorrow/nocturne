package com.danialrekhman.paymentservicenocturne.kafka;

import com.danialrekhman.commonevents.OrderCreatedEvent;
import com.danialrekhman.paymentservicenocturne.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "order-created",
            groupId = "payment-service-group",
            containerFactory = "orderCreatedKafkaListenerContainerFactory"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        paymentService.createAndProcessPayment(event);
    }
}


