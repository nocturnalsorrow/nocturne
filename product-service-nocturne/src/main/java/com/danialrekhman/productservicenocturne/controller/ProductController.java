package com.danialrekhman.productservicenocturne.controller;

import com.danialrekhman.productservicenocturne.dto.ProductRequestDTO;
import com.danialrekhman.productservicenocturne.dto.ProductResponseDTO;
import com.danialrekhman.productservicenocturne.mapper.ProductMapper;
import com.danialrekhman.productservicenocturne.model.Product;
import com.danialrekhman.productservicenocturne.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productMapper.toDto(product));
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@PathVariable String keyword) {
        return ResponseEntity.ok(
                productService.searchProducts(keyword).stream()
                        .map(productMapper::toDto)
                        .toList());
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok(
                productService.getAllProducts().stream()
                        .map(productMapper::toDto)
                        .toList());
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(
                productService.getProductsByCategory(categoryId).stream()
                        .map(productMapper::toDto)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO dto, Authentication authentication) {
        Product product = productService.createProduct(productMapper.toEntity(dto), authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDto(product));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long productId,
                                                            @RequestBody ProductRequestDTO dto, Authentication authentication) {
        Product updated = productService.updateProduct(productId, productMapper.toEntity(dto), authentication);
        return ResponseEntity.ok(productMapper.toDto(updated));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId, Authentication authentication) {
        productService.deleteProduct(productId, authentication);
        return ResponseEntity.noContent().build();
    }
}