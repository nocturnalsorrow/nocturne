package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.model.Category;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category, Authentication authentication);

    Category updateCategory(Long id, Category updatedCategory, Authentication authentication);

    void deleteCategory(Long id, Authentication authentication);

    Category getCategoryById(Long id, Authentication authentication);

    List<Category> getAllCategories();

    List<Category> getSubcategories(Long parentId);

    List<Category> getAllParentCategories();
}