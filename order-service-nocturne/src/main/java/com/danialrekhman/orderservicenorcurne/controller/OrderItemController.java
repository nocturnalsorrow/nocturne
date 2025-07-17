package com.danialrekhman.orderservicenorcurne.controller;

import com.danialrekhman.orderservicenorcurne.dto.OrderItemRequestDTO;
import com.danialrekhman.orderservicenorcurne.dto.OrderItemResponseDTO;
import com.danialrekhman.orderservicenorcurne.mapper.OrderMapper;
import com.danialrekhman.orderservicenorcurne.model.OrderItem;
import com.danialrekhman.orderservicenorcurne.service.OrderItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderItemController {

    OrderItemService orderItemService;
    OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderItemResponseDTO> createItem(@RequestBody OrderItemRequestDTO dto, Authentication authentication) {
        OrderItem item = orderItemService.createOrderItem(orderMapper.toEntity(dto), authentication);
        return new ResponseEntity<>(orderMapper.toDto(item), HttpStatus.CREATED);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<OrderItemResponseDTO> getItem(@PathVariable Long itemId, Authentication authentication) {
        OrderItem item = orderItemService.getOrderItemById(itemId, authentication);
        return ResponseEntity.ok(orderMapper.toDto(item));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponseDTO>> getItemsByOrder(@PathVariable Long orderId, Authentication authentication) {
        List<OrderItem> items = orderItemService.getItemsByOrderId(orderId, authentication);
        List<OrderItemResponseDTO> response = items.stream()
                .map(orderMapper::toDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{itemId}/quantity")
    public ResponseEntity<OrderItemResponseDTO> updateQuantity(@PathVariable Long itemId, @RequestParam int quantity, Authentication authentication) {
        OrderItem updatedItem = orderItemService.updateOrderItemQuantity(itemId, quantity, authentication);
        return ResponseEntity.ok(orderMapper.toDto(updatedItem));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId, Authentication authentication) {
        orderItemService.deleteOrderItem(itemId, authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<Void> deleteItemsByOrder(@PathVariable Long orderId, Authentication authentication) {
        orderItemService.deleteItemsByOrderId(orderId, authentication);
        return ResponseEntity.noContent().build();
    }
}
