package com.danialrekhman.orderservicenorcurne.service;

import com.danialrekhman.orderservicenorcurne.exception.*;
import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import com.danialrekhman.orderservicenorcurne.repository.OrderItemRepository;
import com.danialrekhman.orderservicenorcurne.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private Order order;
    private OrderItem orderItem;
    private final String USER_EMAIL = "user@example.com";
    private final Long ORDER_ID = 1L;
    private final Long ORDER_ITEM_ID = 10L;
    private final Long PRODUCT_ID = 101L;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setUserEmail(USER_EMAIL);

        orderItem = new OrderItem();
        orderItem.setId(ORDER_ITEM_ID);
        orderItem.setOrder(order);
        orderItem.setProductId(PRODUCT_ID);
        orderItem.setQuantity(2);
        orderItem.setPriceAtOrder(new BigDecimal("50.00"));
    }

    private void mockUserName() {
        when(authentication.getName()).thenReturn(USER_EMAIL);
    }

    private void mockUserRole(boolean isAdmin) {
        if (isAdmin) {
            when(authentication.getAuthorities()).thenAnswer(invocation ->
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else {
            when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        }
    }

    @Test
    void createOrderItem_AsAdmin_Success() {
        mockUserRole(true);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        OrderItem createdItem = orderItemService.createOrderItem(orderItem, authentication);

        assertNotNull(createdItem);
        assertEquals(ORDER_ITEM_ID, createdItem.getId());
        verify(orderRepository).findById(ORDER_ID);
        verify(orderItemRepository).save(orderItem);
    }

    @Test
    void createOrderItem_AsNonAdmin_ThrowsAccessDenied() {
        mockUserRole(false); // Потрібна лише роль для перевірки isAdmin
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(CustomAccessDeniedException.class, () ->
                orderItemService.createOrderItem(orderItem, authentication));
        verify(orderRepository).findById(ORDER_ID);
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void createOrderItem_WithNonExistentOrder_ThrowsOrderNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                orderItemService.createOrderItem(orderItem, authentication));

        verify(orderRepository).findById(ORDER_ID);
        verify(orderItemRepository, never()).save(any());
        verifyNoInteractions(authentication);
    }

    @Test
    void createOrderItem_WithZeroQuantity_ThrowsInvalidQuantity() {
        mockUserRole(true);
        orderItem.setQuantity(0);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(InvalidQuantityException.class, () ->
                orderItemService.createOrderItem(orderItem, authentication));
        verify(orderRepository).findById(ORDER_ID);
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void getOrderItemById_AsOwner_Success() {
        mockUserName();
        mockUserRole(false);
        when(orderItemRepository.findById(ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));

        OrderItem foundItem = orderItemService.getOrderItemById(ORDER_ITEM_ID, authentication);

        assertNotNull(foundItem);
        assertEquals(orderItem.getId(), foundItem.getId());
        verify(orderItemRepository).findById(ORDER_ITEM_ID);
    }

    @Test
    void getOrderItemById_AsAdmin_Success() {
        mockUserRole(true);
        when(orderItemRepository.findById(ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));

        OrderItem foundItem = orderItemService.getOrderItemById(ORDER_ITEM_ID, authentication);

        assertNotNull(foundItem);
        verify(orderItemRepository).findById(ORDER_ITEM_ID);
    }

    @Test
    void getOrderItemById_AsDifferentUser_ThrowsAccessDenied() {
        when(authentication.getName()).thenReturn("another@user.com");
        mockUserRole(false);
        when(orderItemRepository.findById(ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));

        assertThrows(CustomAccessDeniedException.class, () ->
                orderItemService.getOrderItemById(ORDER_ITEM_ID, authentication));
    }

    @Test
    void getOrderItemById_NotFound_ThrowsOrderItemNotFound() {
        when(orderItemRepository.findById(ORDER_ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrderItemNotFoundException.class, () ->
                orderItemService.getOrderItemById(ORDER_ITEM_ID, authentication));
    }

    @Test
    void getItemsByOrderId_AsOwner_Success() {
        mockUserName();
        mockUserRole(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(orderItem));

        List<OrderItem> items = orderItemService.getItemsByOrderId(ORDER_ID, authentication);

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
    }

    @Test
    void getItemsByOrderId_OrderNotFound_ThrowsOrderNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                orderItemService.getItemsByOrderId(ORDER_ID, authentication));
    }

    @Test
    void updateOrderItemQuantity_AsAdmin_Success() {
        mockUserRole(true);
        int newQuantity = 5;
        when(orderItemRepository.findById(ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem updatedItem = orderItemService.updateOrderItemQuantity(ORDER_ITEM_ID, newQuantity, authentication);

        assertEquals(newQuantity, updatedItem.getQuantity());
        verify(orderItemRepository).findById(ORDER_ITEM_ID);
        verify(orderItemRepository).save(orderItem);
    }

    @Test
    void updateOrderItemQuantity_AsNonAdmin_ThrowsAccessDenied() {
        mockUserRole(false);

        assertThrows(CustomAccessDeniedException.class, () ->
                orderItemService.updateOrderItemQuantity(ORDER_ITEM_ID, 5, authentication));
        verifyNoInteractions(orderItemRepository, orderRepository);
    }

    @Test
    void deleteOrderItem_AsAdmin_Success() {
        mockUserRole(true);
        when(orderItemRepository.findById(ORDER_ITEM_ID)).thenReturn(Optional.of(orderItem));
        doNothing().when(orderItemRepository).deleteById(ORDER_ITEM_ID);

        orderItemService.deleteOrderItem(ORDER_ITEM_ID, authentication);

        verify(orderItemRepository).findById(ORDER_ITEM_ID);
        verify(orderItemRepository).deleteById(ORDER_ITEM_ID);
    }

    @Test
    void deleteOrderItem_AsNonAdmin_ThrowsAccessDenied() {
        mockUserRole(false);

        assertThrows(CustomAccessDeniedException.class, () ->
                orderItemService.deleteOrderItem(ORDER_ITEM_ID, authentication));
        verifyNoInteractions(orderItemRepository, orderRepository);
    }
}