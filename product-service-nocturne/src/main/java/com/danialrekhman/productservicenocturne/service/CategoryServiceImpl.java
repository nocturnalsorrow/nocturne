package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.productservicenocturne.exception.DuplicateResourceException;
import com.danialrekhman.productservicenocturne.exception.ResourceNotFoundException;
import com.danialrekhman.productservicenocturne.model.Category;
import com.danialrekhman.productservicenocturne.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public Category createCategory(Category category, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can create category.");
        if (categoryRepository.existsByName(category.getName()))
            throw new DuplicateResourceException("Category with name '" + category.getName() + "' already exists.");
        if (category.getSubcategories() != null)
            category.getSubcategories().forEach(sub -> sub.setParent(category));
        return categoryRepository.save(category);
    }

    @Transactional
    @Override
    public Category updateCategory(Long id, Category updatedCategory, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can update category.");
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found."));
        if (!existingCategory.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName()))
            throw new DuplicateResourceException("Category with name '" + updatedCategory.getName() + "' already exists.");
        existingCategory.setName(updatedCategory.getName());
        if (updatedCategory.getParent() != null && updatedCategory.getParent().getId() != null) {
            if (updatedCategory.getParent().getId().equals(id))
                throw new IllegalArgumentException("Category cannot be a parent of itself. Parent ID: " + id + ". Category ID: " + id + ".");
            Category parent = categoryRepository.findById(updatedCategory.getParent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category with id " + updatedCategory.getParent().getId() + " not found."));
            existingCategory.setParent(parent);
        } else existingCategory.setParent(null);
        return categoryRepository.save(existingCategory);
    }

    @Transactional
    @Override
    public void deleteCategory(Long id, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can delete category.");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found."));
        categoryRepository.delete(category);
    }

    @Override
    public Category getCategoryById(Long id, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can retrieve category.");
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found."));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getSubcategories(Long parentId) {
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found."));
        return categoryRepository.findAllByParentId(parentId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Category> getAllParentCategories() {
        return categoryRepository.findAllByParentIsNull();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}