package com.danialrekhman.productservicenocturne.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CategoryRequestDTO {
    private String name;
    private Long parentId;
}