package com.danialrekhman.orderservicenorcurne.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequestDTO {
    String userEmail;
    List<OrderItemRequestDTO> items;
}
