package com.danialrekhman.orderservicenorcurne.dto;

import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderUpdateStatusDTO {
    OrderStatus status;
}
