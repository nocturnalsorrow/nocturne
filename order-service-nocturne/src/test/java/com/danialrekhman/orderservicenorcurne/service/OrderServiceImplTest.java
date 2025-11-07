package com.danialrekhman.orderservicenorcurne.service;

import com.danialrekhman.commonevents.OrderCreatedEvent;
import com.danialrekhman.commonevents.PaymentFailedEvent;
import com.danialrekhman.commonevents.PaymentProcessedEvent;
import com.danialrekhman.commonevents.ProductCheckMessage;
import com.danialrekhman.orderservicenorcurne.dto.OrderItemRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderRequestDTO;
import com.danialrekhman.orderservicenorcurne.exception.CustomAccessDeniedException;
import com.danialrekhman.orderservicenorcurne.exception.OrderCancellationException;
import com.danialrekhman.orderservicenorcurne.kafka.producer.OrderEventProducer;
import com.danialrekhman.orderservicenorcurne.kafka.producer.ProductCheckProducer;
import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import com.danialrekhman.orderservicenorcurne.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductCheckProducer productCheckProducer;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Order order;
    private final String USER_EMAIL = "test@example.com";
    private final String ADMIN_EMAIL = "admin@example.com";
    private final Long ORDER_ID = 1L;
    private final Long PRODUCT_ID_1 = 101L;
    private final Long PRODUCT_ID_2 = 102L;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setUserEmail(USER_EMAIL);
        order.setStatus(OrderStatus.NEW);
        order.setOrderDate(LocalDateTime.now());

        OrderItem item1 = new OrderItem(1L, PRODUCT_ID_1, 2, new BigDecimal("10.00"), order);
        order.setItems(List.of(item1));
    }

    private void mockUserAuthentication() {
        when(authentication.getName()).thenReturn(USER_EMAIL);
    }

    private void mockAdminAuthentication() {
        when(authentication.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void createOrder_WhenProductsAreAvailable_Success() {
        mockUserAuthentication();

        OrderItemRequestDTO item1DTO = new OrderItemRequestDTO(PRODUCT_ID_1, 2, null);
        OrderRequestDTO requestDTO = new OrderRequestDTO(USER_EMAIL, List.of(item1DTO));

        ProductCheckMessage response = ProductCheckMessage.builder()
                .productId(PRODUCT_ID_1).available(true).priceAtOrder(new BigDecimal("10.00")).quantity(2).build();

        when(productCheckProducer.check(PRODUCT_ID_1, 2)).thenReturn(CompletableFuture.completedFuture(response));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doNothing().when(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));

        Order createdOrder = orderService.createOrder(requestDTO, authentication);

        assertNotNull(createdOrder);
        assertEquals(USER_EMAIL, createdOrder.getUserEmail());
        assertEquals(OrderStatus.NEW, createdOrder.getStatus());

        verify(productCheckProducer, times(1)).check(PRODUCT_ID_1, 2);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).publishOrderCreated(any(OrderCreatedEvent.class));
        verify(productCheckProducer, never()).release(anyLong(), anyInt());
    }

    @Test
    void createOrder_WhenOneProductIsUnavailable_ThrowsExceptionAndReleasesStock() {
        mockUserAuthentication();

        OrderItemRequestDTO item1DTO = new OrderItemRequestDTO(PRODUCT_ID_1, 2, null);
        OrderItemRequestDTO item2DTO = new OrderItemRequestDTO(PRODUCT_ID_2, 1, null);
        OrderRequestDTO requestDTO = new OrderRequestDTO(USER_EMAIL, List.of(item1DTO, item2DTO));

        ProductCheckMessage availableResponse = ProductCheckMessage.builder()
                .productId(PRODUCT_ID_1).available(true).priceAtOrder(new BigDecimal("10.00")).quantity(2).build();
        ProductCheckMessage unavailableResponse = ProductCheckMessage.builder()
                .productId(PRODUCT_ID_2).available(false).quantity(1).build();

        when(productCheckProducer.check(PRODUCT_ID_1, 2)).thenReturn(CompletableFuture.completedFuture(availableResponse));
        when(productCheckProducer.check(PRODUCT_ID_2, 1)).thenReturn(CompletableFuture.completedFuture(unavailableResponse));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(requestDTO, authentication));

        verify(productCheckProducer, times(1)).release(PRODUCT_ID_1, 2);
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventProducer, never()).publishOrderCreated(any());
    }

    @Test
    void createOrder_WhenProductCheckTimesOut_ThrowsException() {
        mockUserAuthentication();

        OrderRequestDTO requestDTO = new OrderRequestDTO(USER_EMAIL, List.of(new OrderItemRequestDTO(PRODUCT_ID_1, 2, null)));

        when(productCheckProducer.check(PRODUCT_ID_1, 2)).thenReturn(CompletableFuture.failedFuture(new TimeoutException("Timeout!")));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(requestDTO, authentication));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_AsOwner_Success() {
        mockUserAuthentication();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        Order foundOrder = orderService.getOrderById(ORDER_ID, authentication);

        assertNotNull(foundOrder);
        assertEquals(ORDER_ID, foundOrder.getId());
        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    void getOrderById_AsAdmin_Success() {
        mockAdminAuthentication();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        Order foundOrder = orderService.getOrderById(ORDER_ID, authentication);

        assertNotNull(foundOrder);
        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    void getOrderById_AsDifferentUser_ThrowsAccessDenied() {
        when(authentication.getName()).thenReturn("other.user@example.com");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(CustomAccessDeniedException.class, () -> orderService.getOrderById(ORDER_ID, authentication));
    }

    @Test
    void handlePaymentResult_SetsStatusToPaid() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .orderId(ORDER_ID)
                .build();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.handlePaymentResult(event);

        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.PAID, orderCaptor.getValue().getStatus());
    }

    @Test
    void handlePaymentFailed_SetsStatusToWaitingForPayment() {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(ORDER_ID)
                .reason("Insufficient funds")
                .build();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.handlePaymentFailed(event);

        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.WAITING_FOR_PAYMENT, orderCaptor.getValue().getStatus());
    }

    @Test
    void cancelOrder_AsOwner_Success() {
        mockUserAuthentication();
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.cancelOrder(ORDER_ID, authentication);

        verify(productCheckProducer).release(PRODUCT_ID_1, 2);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.CANCELLED, orderCaptor.getValue().getStatus());
    }

    @Test
    void cancelOrder_WhenDelivered_ThrowsException() {
        mockUserAuthentication();
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(OrderCancellationException.class, () -> orderService.cancelOrder(ORDER_ID, authentication));

        verify(productCheckProducer, never()).release(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }
}