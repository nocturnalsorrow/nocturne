package com.danialrekhman.orderservicenorcurne.service;

import com.danialrekhman.orderservicenorcurne.exception.*;
import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import com.danialrekhman.orderservicenorcurne.repository.OrderItemRepository;
import com.danialrekhman.orderservicenorcurne.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    OrderItemRepository orderItemRepository;
    OrderRepository orderRepository;

    @Transactional
    @Override
    public OrderItem createOrderItem(OrderItem item, Authentication authentication) {
        Long orderId = Optional.ofNullable(item.getOrder())
                .map(Order::getId)
                .orElseThrow(() -> new InvalidOrderItemDataException("Order must be provided for order item."));
        orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found."));
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("You don't have access to create an item for this order.");
        if (item.getProductId() == null)
            throw new InvalidOrderItemDataException("Product ID must be provided.");
        if (item.getQuantity() <= 0)
            throw new InvalidQuantityException("Quantity must be greater than 0.");
        return orderItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long orderItemId, Authentication authentication) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemNotFoundException("No order item found with ID: " + orderItemId));
        Order order = orderItem.getOrder();
        if (order == null)
            throw new OrderNotFoundException("Associated order not found for order item ID: " + orderItemId);
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You don't have access to this order item.");
        return orderItem;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByOrderId(Long orderId, Authentication authentication) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("No order found with ID: " + orderId));
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You don't have access to items of this order.");
        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        if (items.isEmpty())
            throw new OrderItemNotFoundException("No items found for order with ID: " + orderId);
        return items;
    }

    @Transactional
    @Override
    public OrderItem updateOrderItemQuantity(Long orderItemId, int newQuantity, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("You don't have access to update quantity of this order item.");
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemNotFoundException("No order item found with ID: " + orderItemId));
        Order order = orderItem.getOrder();
        if (order == null)
            throw new OrderNotFoundException("Associated order not found for order item ID: " + orderItemId);
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You don't have access to update this order item.");
        if (newQuantity <= 0)
            throw new InvalidQuantityException("Quantity must be greater than 0.");
        orderItem.setQuantity(newQuantity);
        return orderItemRepository.save(orderItem);
    }

    @Transactional
    @Override
    public void deleteOrderItem(Long orderItemId, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("You don't have access to delete this order item.");
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemNotFoundException("No order item found with ID: " + orderItemId));
        Order order = orderItem.getOrder();
        if (order == null)
            throw new OrderNotFoundException("Associated order not found for order item ID: " + orderItemId);
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You don't have access to delete this order item.");
        orderItemRepository.deleteById(orderItemId);
    }

    @Override
    public void deleteItemsByOrderId(Long orderId, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("You don't have access to delete items of this order.");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("No order found with ID: " + orderId));
        if (!isAdmin(authentication) && !order.getUserEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You don't have access to delete items of this order.");
        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        if (items.isEmpty())
            throw new OrderItemNotFoundException("No items found to delete for order with ID: " + orderId);
        orderItemRepository.deleteByOrderId(orderId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}
