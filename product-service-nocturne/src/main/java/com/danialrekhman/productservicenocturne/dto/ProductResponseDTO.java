package com.danialrekhman.productservicenocturne.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponseDTO {
    Long id;
    String name;
    String description;
    BigDecimal price;
    Boolean available;
    Long categoryId;
    String categoryName;
    Integer quantity;
    List<ProductImageDTO> images;
}