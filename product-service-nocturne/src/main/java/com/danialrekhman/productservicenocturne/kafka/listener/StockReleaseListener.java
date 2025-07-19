package com.danialrekhman.productservicenocturne.kafka.listener;

import com.danialrekhman.commonevents.ProductCheckMessage;
import com.danialrekhman.productservicenocturne.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockReleaseListener {

    private final ProductService productService;

    @KafkaListener(topics = "stock-release", groupId = "product-service-group")
    public void onRelease(ProductCheckMessage request) {
        productService.releaseStock(request.getProductId(), request.getQuantity());
    }
}

