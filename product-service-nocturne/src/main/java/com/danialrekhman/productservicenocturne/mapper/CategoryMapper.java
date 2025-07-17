package com.danialrekhman.productservicenocturne.mapper;

import com.danialrekhman.productservicenocturne.dto.CategoryRequestDTO;
import com.danialrekhman.productservicenocturne.dto.CategoryResponseDTO;
import com.danialrekhman.productservicenocturne.model.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toDto(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .subcategories(category.getSubcategories() != null
                        ? category.getSubcategories().stream()
                        .map(this::toDto)
                        .toList()
                        : new ArrayList<>())
                .build();
    }

    public Category toEntity(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        if (dto.getParentId() != null) {
            Category parent = new Category();
            parent.setId(dto.getParentId());
            category.setParent(parent);
        }
        return category;
    }
}

