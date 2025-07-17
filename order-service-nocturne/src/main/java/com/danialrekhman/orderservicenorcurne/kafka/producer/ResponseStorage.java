package com.danialrekhman.orderservicenorcurne.kafka.producer;

import com.danialrekhman.commonevents.ProductCheckMessage;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResponseStorage {
    private final Map<String, CompletableFuture<ProductCheckMessage>> waiting = new ConcurrentHashMap<>();
    public void register(String id, CompletableFuture<ProductCheckMessage> future) {
        waiting.put(id, future);
    }
    public void complete(String id, ProductCheckMessage resp) {
        Optional.ofNullable(waiting.remove(id)).ifPresent(f -> f.complete(resp));
    }
}

