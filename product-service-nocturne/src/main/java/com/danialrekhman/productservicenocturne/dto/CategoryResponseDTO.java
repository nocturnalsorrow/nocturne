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
    private Long parentId; // чтобы не возвращать весь объект parent, а только id
    private List<CategoryResponseDTO> subcategories; // вложенные подкатегории (можно пустой список)
}