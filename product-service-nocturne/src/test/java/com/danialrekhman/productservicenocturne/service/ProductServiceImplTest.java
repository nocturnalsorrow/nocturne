package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.productservicenocturne.exception.DuplicateResourceException;
import com.danialrekhman.productservicenocturne.exception.ResourceNotFoundException;
import com.danialrekhman.productservicenocturne.model.Category;
import com.danialrekhman.productservicenocturne.model.Product;
import com.danialrekhman.productservicenocturne.repository.CategoryRepository;
import com.danialrekhman.productservicenocturne.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Guitars");

        product = new Product();
        product.setId(1L);
        product.setName("Fender Stratocaster");
        product.setDescription("A classic electric guitar.");
        product.setPrice(new BigDecimal("1200.00"));
        product.setCategory(category);
        product.setQuantity(10);
        product.setAvailable(true);
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
    void createProduct_AsAdmin_Success() {
        mockAdminAuthentication();
        when(productRepository.existsByName(product.getName())).thenReturn(false);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product createdProduct = productService.createProduct(product, authentication);

        assertNotNull(createdProduct);
        assertEquals("Fender Stratocaster", createdProduct.getName());
        verify(productRepository).existsByName(product.getName());
        verify(categoryRepository).findById(category.getId());
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();

        assertThrows(CustomAccessDeniedException.class, () ->
                productService.createProduct(product, authentication));
        verifyNoInteractions(productRepository, categoryRepository);
    }

    @Test
    void createProduct_WithExistingName_ThrowsDuplicateResourceException() {
        mockAdminAuthentication();
        when(productRepository.existsByName(product.getName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                productService.createProduct(product, authentication));
        verify(productRepository).existsByName(product.getName());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithNegativePrice_ThrowsIllegalArgumentException() {
        mockAdminAuthentication();

        product.setPrice(new BigDecimal("-100"));

        when(productRepository.existsByName(product.getName())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct(product, authentication));

        verify(productRepository).existsByName(product.getName());
        verify(productRepository, never()).save(any(Product.class));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void createProduct_WithNonExistentCategory_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        when(productRepository.existsByName(product.getName())).thenReturn(false);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(product, authentication));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_AsAdmin_Success() {
        mockAdminAuthentication();
        Product updatedDetails = new Product();
        updatedDetails.setName("Gibson Les Paul");
        updatedDetails.setPrice(new BigDecimal("2500.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product updatedProduct = productService.updateProduct(1L, updatedDetails, authentication);

        assertNotNull(updatedProduct);
        assertEquals("Gibson Les Paul", updatedProduct.getName());
        assertEquals(0, new BigDecimal("2500.00").compareTo(updatedProduct.getPrice()));
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_ProductNotFound_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.updateProduct(1L, new Product(), authentication));
    }

    @Test
    void updateProduct_ToExistingName_ThrowsDuplicateResourceException() {
        mockAdminAuthentication();
        Product updatedDetails = new Product();
        updatedDetails.setName("Gibson Les Paul");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                productService.updateProduct(1L, updatedDetails, authentication));
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_AsAdmin_Success() {
        mockAdminAuthentication();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L, authentication);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();
        assertThrows(CustomAccessDeniedException.class, () -> productService.deleteProduct(1L, authentication));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product found = productService.getProductById(1L);

        assertNotNull(found);
        assertEquals(product.getName(), found.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsResourceNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void getProductsByCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));

        List<Product> products = productService.getProductsByCategory(1L);

        assertNotNull(products);
        assertEquals(1, products.size());
        verify(categoryRepository).findById(1L);
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    void getProductsByCategory_CategoryNotFound_ThrowsResourceNotFoundException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductsByCategory(1L));
        verify(productRepository, never()).findByCategoryId(anyLong());
    }

    @Test
    void reserveStock_WhenSufficient_ReturnsTrue() {
        when(productRepository.decreaseStock(1L, 5)).thenReturn(1); // 1 означає, що 1 рядок було оновлено

        boolean result = productService.reserveStock(1L, 5);

        assertTrue(result);
        verify(productRepository).decreaseStock(1L, 5);
    }

    @Test
    void reserveStock_WhenInsufficient_ReturnsFalse() {
        when(productRepository.decreaseStock(1L, 15)).thenReturn(0); // 0 означає, що жоден рядок не було оновлено

        boolean result = productService.reserveStock(1L, 15);

        assertFalse(result);
        verify(productRepository).decreaseStock(1L, 15);
    }

    @Test
    void releaseStock_CallsIncreaseStock() {
        productService.releaseStock(1L, 5);

        verify(productRepository).increaseStock(1L, 5);
    }
}
