package com.danialrekhman.orderservicenorcurne.kafka.producer;

import com.danialrekhman.commonevents.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send("order-created", String.valueOf(event.getOrderId()), event);
        log.info("Published OrderCreatedEvent for orderId={}", event.getOrderId());
    }
}
