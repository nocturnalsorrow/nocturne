package com.danialrekhman.productservicenocturne.kafka.listener;

import com.danialrekhman.commonevents.ProductCheckMessage;
import com.danialrekhman.productservicenocturne.model.Product;
import com.danialrekhman.productservicenocturne.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class ProductCheckListener {

    private final KafkaTemplate<String, ProductCheckMessage> kafkaTemplate;
    private final ProductService productService;

    @KafkaListener(topics = "product-check", groupId = "product-service-group")
    public void onMessage(ProductCheckMessage request) {
        boolean ok = false;
        BigDecimal price = BigDecimal.ZERO;

        try {
            Product product = productService.getProductById(request.getProductId());
            if (product != null && product.isAvailable() && product.getQuantity() >= request.getQuantity()) {
                if (productService.reserveStock(product.getId(), request.getQuantity())) {
                    ok = true;
                    price = product.getPrice();
                }
            }
        } catch (Exception e) {
            ok = false;
        }

        request.setAvailable(ok);
        request.setPriceAtOrder(price);
        request.setMessage(ok ? "Reserved" : "Not enough stock");

        kafkaTemplate.send("product-check-response", request.getCorrelationId(), request);
    }
}



