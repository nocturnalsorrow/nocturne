package com.danialrekhman.orderservicenorcurne.controller;

import com.danialrekhman.orderservicenorcurne.mapper.OrderMapper;
import com.danialrekhman.orderservicenorcurne.dto.OrderRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderResponseDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderUpdateStatusDTO;
import com.danialrekhman.orderservicenorcurne.model.Order;
import com.danialrekhman.orderservicenorcurne.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(Authentication authentication) {
        List<Order> orders = orderService.getAllOrders(authentication);
        List<OrderResponseDTO> response = orders.stream()
                .map(orderMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId, Authentication authentication) {
        Order order = orderService.getOrderById(orderId, authentication);
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Order> orders = orderService.getOrdersByUserEmail(userEmail, authentication);
        List<OrderResponseDTO> response = orders.stream()
                .map(orderMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO requestDto, Authentication authentication) {
        Order order = orderService.createOrder(requestDto, authentication);
        OrderResponseDTO responseDTO = orderMapper.toDto(order);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long orderId, @RequestBody OrderUpdateStatusDTO statusDTO, Authentication authentication) {
        Order order = orderService.updateOrderStatus(orderId, statusDTO, authentication);
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        Order cancelled = orderService.cancelOrder(orderId, authentication);
        return ResponseEntity.ok(orderMapper.toDto(cancelled));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId, Authentication authentication) {
        orderService.deleteOrder(orderId, authentication);
        return ResponseEntity.noContent().build();
    }
}
