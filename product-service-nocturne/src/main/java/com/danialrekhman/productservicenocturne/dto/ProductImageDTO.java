package com.danialrekhman.productservicenocturne.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductImageDTO {
    Long id;
    String imageUrl;
}