package com.danialrekhman.orderservicenorcurne.service;

import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderItemService {

    OrderItem createOrderItem(OrderItem item, Authentication authentication);

    OrderItem getOrderItemById(Long orderItemId, Authentication authentication);

    List<OrderItem> getItemsByOrderId(Long orderId, Authentication authentication);

    OrderItem updateOrderItemQuantity(Long orderItemId, int newQuantity, Authentication authentication);

    void deleteOrderItem(Long orderItemId, Authentication authentication);

    void deleteItemsByOrderId(Long orderId, Authentication authentication);
}
