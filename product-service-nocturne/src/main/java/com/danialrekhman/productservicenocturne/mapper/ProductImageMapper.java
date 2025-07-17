package com.danialrekhman.productservicenocturne.mapper;

import com.danialrekhman.productservicenocturne.dto.ProductImageDTO;
import com.danialrekhman.productservicenocturne.model.ProductImage;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    public ProductImageDTO toDto(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }

    public ProductImage toEntity(ProductImageDTO dto) {
        return ProductImage.builder()
                .id(dto.getId())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}
