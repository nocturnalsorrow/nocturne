package com.danialrekhman.productservicenocturne.controller;

import com.danialrekhman.productservicenocturne.dto.ProductImageDTO;
import com.danialrekhman.productservicenocturne.mapper.ProductImageMapper;
import com.danialrekhman.productservicenocturne.model.ProductImage;
import com.danialrekhman.productservicenocturne.service.ProductImageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;
    private final ProductImageMapper productImageMapper;

    @PostMapping("/{productId}/images")
    public ResponseEntity<ProductImageDTO> addImageToProduct(
            @PathVariable Long productId,
            @RequestBody ProductImageDTO dto, Authentication authentication) {

        ProductImage image = productImageMapper.toEntity(dto);
        ProductImage saved = productImageService.addImageToProduct(productId, image, authentication);
        return ResponseEntity.ok(productImageMapper.toDto(saved));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId, Authentication authentication) {
        productImageService.deleteImage(imageId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> getImagesByProduct(@PathVariable Long productId, Authentication authentication) {
        return ResponseEntity.ok(
                productImageService.getImagesByProduct(productId, authentication).stream()
                        .map(productImageMapper::toDto)
                        .toList());
    }
}