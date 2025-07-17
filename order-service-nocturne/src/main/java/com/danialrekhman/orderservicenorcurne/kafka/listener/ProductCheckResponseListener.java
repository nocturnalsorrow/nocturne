package com.danialrekhman.orderservicenorcurne.kafka.listener;

import com.danialrekhman.commonevents.ProductCheckMessage;
import com.danialrekhman.orderservicenorcurne.kafka.producer.ResponseStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class ProductCheckResponseListener {

    private final ResponseStorage storage;

    @KafkaListener(topics="product-check-response",
            groupId="order-service-group",
            containerFactory="kafkaListenerContainerFactory")
    public void on(ProductCheckMessage resp, Acknowledgment ack){
        storage.complete(resp.getCorrelationId(), resp);
        ack.acknowledge();
    }
}