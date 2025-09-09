package com.danialrekhman.orderservicenorcurne.dto;

import com.danialrekhman.orderservicenorcurne.model.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderUpdateStatusDTO {
    OrderStatus status;
}
