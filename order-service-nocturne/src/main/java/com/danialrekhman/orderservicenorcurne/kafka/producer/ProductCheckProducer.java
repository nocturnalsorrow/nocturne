package com.danialrekhman.orderservicenorcurne.kafka.producer;

import com.danialrekhman.commonevents.ProductCheckMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductCheckProducer {

    private final KafkaTemplate<String, ProductCheckMessage> template;
    private final ResponseStorage storage;

    public CompletableFuture<ProductCheckMessage> check(Long id, int qty) {
        String corr = UUID.randomUUID().toString();
        ProductCheckMessage msg = ProductCheckMessage.builder()
                .correlationId(corr)
                .productId(id)
                .quantity(qty)
                .available(false)
                .build();

        CompletableFuture<ProductCheckMessage> fut = new CompletableFuture<>();
        storage.register(corr, fut);

        template.send("product-check", msg);
        return fut.orTimeout(3, TimeUnit.SECONDS);
    }

    public void release(Long productId, int qty) {
        ProductCheckMessage msg = ProductCheckMessage.builder()
                .correlationId(UUID.randomUUID().toString())
                .productId(productId)
                .quantity(qty)
                .available(true)
                .message("Release stock")
                .build();

        template.send("stock-release", msg);
    }
}

