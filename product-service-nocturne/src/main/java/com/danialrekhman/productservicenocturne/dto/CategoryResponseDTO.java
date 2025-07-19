package com.danialrekhman.productservicenocturne.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private Long parentId;
    private List<CategoryResponseDTO> subcategories;
}