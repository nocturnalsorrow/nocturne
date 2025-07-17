package com.danialrekhman.productservicenocturne.kafka.listener;

import com.danialrekhman.commonevents.ProductCheckMessage;
import com.danialrekhman.productservicenocturne.model.Product;
import com.danialrekhman.productservicenocturne.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class ProductCheckListener {

    private final KafkaTemplate<String, ProductCheckMessage> kafkaTemplate;
    private final ProductService productService;

    @KafkaListener(topics = "product-check", groupId = "product-service-group")
    public void onMessage(ProductCheckMessage request) {
        Product product = productService.getProductById(request.getProductId());
        boolean ok = product != null && product.isAvailable();

        request.setAvailable(ok);
        request.setMessage(ok ? "Available" : "Not available");

        if (ok) {
            request.setPriceAtOrder(product.getPrice());
        } else {
            request.setPriceAtOrder(BigDecimal.ZERO);
        }

        kafkaTemplate.send("product-check-response", request.getCorrelationId(), request);
    }
}

