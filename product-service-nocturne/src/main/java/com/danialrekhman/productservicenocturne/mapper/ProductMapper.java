package com.danialrekhman.productservicenocturne.mapper;

import com.danialrekhman.productservicenocturne.dto.*;
import com.danialrekhman.productservicenocturne.model.Category;
import com.danialrekhman.productservicenocturne.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductImageMapper productImageMapper;

    public ProductResponseDTO toDto(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .available(product.isAvailable())
                .images(product.getImages() != null
                        ? product.getImages().stream()
                        .map(productImageMapper::toDto)
                        .toList()
                        : new ArrayList<>())
                .build();
    }

    public Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        if (dto.getAvailable() != null)
            product.setAvailable(dto.getAvailable());
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(dto.getCategoryId());
            product.setCategory(category);
        }
        return product;
    }
}


