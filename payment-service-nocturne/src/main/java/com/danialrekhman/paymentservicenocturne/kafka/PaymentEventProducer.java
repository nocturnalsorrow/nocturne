package com.danialrekhman.paymentservicenocturne.kafka;

import com.danialrekhman.commonevents.PaymentFailedEvent;
import com.danialrekhman.commonevents.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        kafkaTemplate.send("payment-processed", String.valueOf(event.getOrderId()), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send("payment-failed", String.valueOf(event.getOrderId()), event);
    }
}
