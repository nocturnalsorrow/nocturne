package com.danialrekhman.orderservicenorcurne.service;

import com.danialrekhman.commonevents.ProductCheckMessage;
import com.danialrekhman.orderservicenorcurne.dto.OrderItemRequestDTO;
import com.danialrekhman.orderservicenorcurne.exception.CustomAccessDeniedException;
import com.danialrekhman.orderservicenorcurne.exception.OrderCancellationException;
import com.danialrekhman.orderservicenorcurne.exception.OrderNotFoundException;
import com.danialrekhman.orderservicenorcurne.kafka.listener.ProductCheckResponseListener;
import com.danialrekhman.orderservicenorcurne.kafka.producer.ProductCheckProducer;
import com.danialrekhman.orderservicenorcurne.mapper.OrderMapper;
import com.danialrekhman.orderservicenorcurne.dto.OrderRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderUpdateStatusDTO;
import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import com.danialrekhman.orderservicenorcurne.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductCheckProducer productCheckProducer;
    private final ProductCheckResponseListener responseListener;

//    @Override
//    public Order createOrder(OrderRequestDTO requestDTO, Authentication authentication) {
//        if (authentication == null || authentication.getName() == null)
//            throw new CustomAccessDeniedException("You don't have access to create an order.");
//        Order order = orderMapper.toEntity(requestDTO);
//        order.setUserEmail(authentication.getName());
//        return orderRepository.save(order);
//    }

    @Override
    public Order createOrder(OrderRequestDTO requestDTO, Authentication authentication) {
        if (authentication == null || authentication.getName() == null)
            throw new CustomAccessDeniedException("You don't have access to create an order.");
        // 1. Отправляем запросы проверки товаров параллельно и собираем futures
        List<CompletableFuture<ProductCheckMessage>> futures = new ArrayList<>();
        for (OrderItemRequestDTO item : requestDTO.getItems()) {
            CompletableFuture<ProductCheckMessage> future = productCheckProducer.check(
                    item.getProductId(), item.getQuantity()
            );
            futures.add(future);
        }
        // 2. Ждём все ответы и собираем их в карту для удобного доступа по productId
        Map<Long, ProductCheckMessage> responsesMap = new HashMap<>();
        for (CompletableFuture<ProductCheckMessage> future : futures) {
            try {
                ProductCheckMessage response = future.get(3, TimeUnit.SECONDS);

                if (!Boolean.TRUE.equals(response.getAvailable())) {
                    throw new RuntimeException("Product with ID " + response.getProductId() + " is not available.");
                }
                responsesMap.put(response.getProductId(), response);

            } catch (InterruptedException | ExecutionException | TimeoutException |
                     java.util.concurrent.TimeoutException e) {
                throw new RuntimeException("Failed to check product availability", e);
            }
        }
        // 3. Создаём сущность заказа и проставляем userEmail
        Order order = new Order();
        order.setUserEmail(authentication.getName());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        // 4. Создаём элементы заказа с актуальными ценами из ответа
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
            ProductCheckMessage productResponse = responsesMap.get(itemDTO.getProductId());
            if (productResponse == null)
                throw new RuntimeException("No response for product ID " + itemDTO.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPriceAtOrder(productResponse.getPriceAtOrder());
            orderItem.setOrder(order);
            items.add(orderItem);
        }
        order.setItems(items);
        // 5. Сохраняем заказ с элементами
        return orderRepository.save(order);
    }


    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, Authentication authentication) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found."));
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You don't have access to this order.");
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserEmail(String email, Authentication authentication) {
        if (!isAdmin(authentication) && !authentication.getName().equals(email))
            throw new CustomAccessDeniedException("You don't have access to this order.");
        return orderRepository.findAllByUserEmail(email);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderUpdateStatusDTO status, Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can update order status.");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found for status update."));
        order.setStatus(status.getStatus());
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, Authentication authentication) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found for cancellation."));
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("Only admin or order owner can cancel order.");
        if (order.getStatus() == OrderStatus.DELIVERED)
            throw new OrderCancellationException("You can't cancel delivered order. " +
                    "Try to contact with customer service if you need to cancel it.");
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long orderId, Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can delete order.");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found for deletion."));
        orderRepository.delete(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders(Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can get all orders.");
        return orderRepository.findAll();
    }

    @Override
    public boolean isOrderPaid(Long orderId, Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can check order payment status.");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found for payment check."));
        return order.getStatus() == OrderStatus.PAID;
    }

    @Override
    public String getUserEmailByOrderId(Long orderId, Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can get user email by order id.");
        return orderRepository.findUserEmailById(orderId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
