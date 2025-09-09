package com.danialrekhman.paymentservicenocturne.dto;

import com.danialrekhman.paymentservicenocturne.model.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequestDTO {
    Long orderId;
    PaymentMethod method;
}