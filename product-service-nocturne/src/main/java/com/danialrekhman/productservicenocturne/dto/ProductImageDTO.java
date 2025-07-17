package com.danialrekhman.productservicenocturne.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
}