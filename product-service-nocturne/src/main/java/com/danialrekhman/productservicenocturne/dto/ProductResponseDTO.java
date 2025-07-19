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
    private Boolean available;
    private Long categoryId;
    private String categoryName;
    private Integer quantity;
    private List<ProductImageDTO> images;
}