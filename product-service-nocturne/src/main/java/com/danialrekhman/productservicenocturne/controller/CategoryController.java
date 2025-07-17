package com.danialrekhman.productservicenocturne.controller;

import com.danialrekhman.productservicenocturne.dto.CategoryRequestDTO;
import com.danialrekhman.productservicenocturne.dto.CategoryResponseDTO;
import com.danialrekhman.productservicenocturne.mapper.CategoryMapper;
import com.danialrekhman.productservicenocturne.model.Category;
import com.danialrekhman.productservicenocturne.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @GetMapping("/parent/{parentId}")
    public List<CategoryResponseDTO> getSubcategories(@PathVariable Long parentId) {
        return categoryService.getSubcategories(parentId).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id, Authentication authentication) {
        Category category = categoryService.getCategoryById(id, authentication);
        if (category == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(categoryMapper.toDto(category));
    }

    @GetMapping("/parents")
    public List<CategoryResponseDTO> getAllParentCategories() {
        return categoryService.getAllParentCategories().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO dto, Authentication authentication) {
        Category created = categoryService.createCategory(categoryMapper.toEntity(dto), authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
                                                              @RequestBody CategoryRequestDTO dto,
                                                              Authentication authentication) {
        Category updated = categoryService.updateCategory(id, categoryMapper.toEntity(dto), authentication);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(categoryMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Authentication authentication) {
        categoryService.deleteCategory(id, authentication);
        return ResponseEntity.noContent().build();
    }
}