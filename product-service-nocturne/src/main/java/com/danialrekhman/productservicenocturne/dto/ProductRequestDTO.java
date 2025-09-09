package com.danialrekhman.productservicenocturne.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequestDTO {
    String name;
    String description;
    BigDecimal price;
    Boolean available;
    Long categoryId;
    Integer quantity;
}