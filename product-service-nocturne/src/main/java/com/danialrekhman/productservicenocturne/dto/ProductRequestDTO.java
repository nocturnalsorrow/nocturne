package com.danialrekhman.productservicenocturne.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private Long categoryId;
    private Integer quantity;
}