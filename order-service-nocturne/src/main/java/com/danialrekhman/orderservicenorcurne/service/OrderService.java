package com.danialrekhman.orderservicenorcurne.service;

import com.danialrekhman.orderservicenorcurne.dto.OrderRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderUpdateStatusDTO;
import com.danialrekhman.orderservicenorcurne.model.Order;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderService {

    Order createOrder(OrderRequestDTO requestDTO, Authentication authentication);

    Order getOrderById(Long orderId,  Authentication authentication);

    List<Order> getOrdersByUserEmail(String email, Authentication authentication);

    Order updateOrderStatus(Long orderId, OrderUpdateStatusDTO status, Authentication authentication);

    Order cancelOrder(Long orderId, Authentication authentication);

    void deleteOrder(Long orderId, Authentication authentication);

    List<Order> getAllOrders(Authentication authentication);

    boolean isOrderPaid(Long orderId, Authentication authentication);

    String getUserEmailByOrderId(Long orderId, Authentication authentication);
}
