package com.danialrekhman.productservicenocturne.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName; // Дополнительно можно выводить имя категории
    private Boolean available;
    private List<ProductImageDTO> images;
}