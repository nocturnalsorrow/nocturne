package com.danialrekhman.orderservicenorcurne.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderRequestDTO {
    String userEmail;
    List<OrderItemRequestDTO> items;
}
