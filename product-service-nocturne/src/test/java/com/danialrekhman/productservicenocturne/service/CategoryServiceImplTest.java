package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.productservicenocturne.exception.DuplicateResourceException;
import com.danialrekhman.productservicenocturne.exception.ResourceNotFoundException;
import com.danialrekhman.productservicenocturne.model.Category;
import com.danialrekhman.productservicenocturne.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Instruments");
    }

    private void mockAdminAuthentication() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private void mockUserAuthentication() {
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void createCategory_AsAdmin_Success() {
        mockAdminAuthentication();
        when(categoryRepository.existsByName("Instruments")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category createdCategory = categoryService.createCategory(category, authentication);

        assertNotNull(createdCategory);
        assertEquals("Instruments", createdCategory.getName());
        verify(categoryRepository, times(1)).existsByName("Instruments");
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void createCategory_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();

        assertThrows(CustomAccessDeniedException.class, () ->
                categoryService.createCategory(category, authentication));

        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_WithExistingName_ThrowsDuplicateResourceException() {
        mockAdminAuthentication();

        when(categoryRepository.existsByName(category.getName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(category, authentication));

        verify(categoryRepository, times(1)).existsByName(category.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_AsAdmin_Success() {
        mockAdminAuthentication();
        Category updatedDetails = new Category();
        updatedDetails.setName("Keys");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Keys")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category updatedCategory = categoryService.updateCategory(1L, updatedDetails, authentication);

        assertNotNull(updatedCategory);
        assertEquals("Keys", updatedCategory.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void updateCategory_NotFound_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        Category updatedDetails = new Category();
        updatedDetails.setName("Keys");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.updateCategory(1L, updatedDetails, authentication));

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();
        Category updatedDetails = new Category();

        assertThrows(CustomAccessDeniedException.class, () ->
                categoryService.updateCategory(1L, updatedDetails, authentication));
    }


    @Test
    void deleteCategory_AsAdmin_Success() {
        mockAdminAuthentication();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        categoryService.deleteCategory(1L, authentication);

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategory_NotFound_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.deleteCategory(1L, authentication));

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void getCategoryById_AsAdmin_Success() {
        mockAdminAuthentication();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category foundCategory = categoryService.getCategoryById(1L, authentication);

        assertNotNull(foundCategory);
        assertEquals(1L, foundCategory.getId());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();

        assertThrows(CustomAccessDeniedException.class, () ->
                categoryService.getCategoryById(1L, authentication));
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> categories = categoryService.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getSubcategories_Success() {
        Category subcategory = new Category();
        subcategory.setId(2L);
        subcategory.setName("Keys");
        subcategory.setParent(category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findAllByParentId(1L)).thenReturn(List.of(subcategory));

        List<Category> subcategories = categoryService.getSubcategories(1L);

        assertNotNull(subcategories);
        assertEquals(1, subcategories.size());
        assertEquals("Keys", subcategories.get(0).getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findAllByParentId(1L);
    }

    @Test
    void getSubcategories_ParentNotFound_ThrowsResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getSubcategories(1L));

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).findAllByParentId(anyLong());
    }

    @Test
    void getAllParentCategories_Success() {
        category.setParent(null);
        when(categoryRepository.findAllByParentIsNull()).thenReturn(List.of(category));

        List<Category> parentCategories = categoryService.getAllParentCategories();

        assertNotNull(parentCategories);
        assertEquals(1, parentCategories.size());
        assertNull(parentCategories.get(0).getParent());
        verify(categoryRepository, times(1)).findAllByParentIsNull();
    }
}
