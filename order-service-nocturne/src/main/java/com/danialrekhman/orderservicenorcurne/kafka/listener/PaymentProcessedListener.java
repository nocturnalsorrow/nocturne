package com.danialrekhman.orderservicenorcurne.kafka.listener;

import com.danialrekhman.commonevents.PaymentFailedEvent;
import com.danialrekhman.commonevents.PaymentProcessedEvent;
import com.danialrekhman.orderservicenorcurne.service.OrderService;
import com.danialrekhman.orderservicenorcurne.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessedListener {

    private final OrderServiceImpl orderService;

    @KafkaListener(
            topics = "payment-processed",
            groupId = "order-service-group",
            containerFactory = "paymentProcessedKafkaListenerContainerFactory"
    )
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        orderService.handlePaymentResult(event);
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-service-group",
            containerFactory = "paymentFailedKafkaListenerContainerFactory"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderService.handlePaymentFailed(event);
    }
}

