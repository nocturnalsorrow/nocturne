package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.productservicenocturne.exception.ResourceNotFoundException;
import com.danialrekhman.productservicenocturne.model.Product;
import com.danialrekhman.productservicenocturne.model.ProductImage;
import com.danialrekhman.productservicenocturne.repository.ProductImageRepository;
import com.danialrekhman.productservicenocturne.repository.ProductRepository;
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
class ProductImageServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private Product product;
    private ProductImage productImage;
    private final Long productId = 1L;
    private final Long imageId = 101L;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(productId);
        product.setName("Fender Stratocaster");

        productImage = new ProductImage();
        productImage.setId(imageId);
        productImage.setImageUrl("http://example.com/image.jpg");
        productImage.setProduct(product);
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
    void addImageToProduct_AsAdmin_Success() {
        mockAdminAuthentication();
        ProductImage newImage = new ProductImage();
        newImage.setImageUrl("http://example.com/new_image.jpg");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(productImage);

        ProductImage savedImage = productImageService.addImageToProduct(productId, newImage, authentication);

        assertNotNull(savedImage);
        assertNotNull(savedImage.getProduct());
        assertEquals(productId, savedImage.getProduct().getId());
        verify(productRepository, times(1)).findById(productId);
        verify(productImageRepository, times(1)).save(newImage);
    }

    @Test
    void addImageToProduct_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();

        assertThrows(CustomAccessDeniedException.class, () ->
                productImageService.addImageToProduct(productId, new ProductImage(), authentication));

        verifyNoInteractions(productRepository, productImageRepository);
    }

    @Test
    void addImageToProduct_ProductNotFound_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productImageService.addImageToProduct(productId, new ProductImage(), authentication));

        verify(productRepository, times(1)).findById(productId);
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void deleteImage_AsAdmin_Success() {
        mockAdminAuthentication();
        when(productImageRepository.findById(imageId)).thenReturn(Optional.of(productImage));
        doNothing().when(productImageRepository).delete(productImage);

        productImageService.deleteImage(imageId, authentication);

        verify(productImageRepository, times(1)).findById(imageId);
        verify(productImageRepository, times(1)).delete(productImage);
    }

    @Test
    void deleteImage_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();

        assertThrows(CustomAccessDeniedException.class, () ->
                productImageService.deleteImage(imageId, authentication));

        verifyNoInteractions(productImageRepository);
    }

    @Test
    void deleteImage_ImageNotFound_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        when(productImageRepository.findById(imageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productImageService.deleteImage(imageId, authentication));

        verify(productImageRepository, times(1)).findById(imageId);
        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void getImagesByProduct_AsAdmin_Success() {
        mockAdminAuthentication();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productImageRepository.findProductImagesByProductId(productId)).thenReturn(List.of(productImage));

        List<ProductImage> images = productImageService.getImagesByProduct(productId, authentication);

        assertNotNull(images);
        assertEquals(1, images.size());
        assertEquals(imageId, images.get(0).getId());
        verify(productRepository, times(1)).existsById(productId);
        verify(productImageRepository, times(1)).findProductImagesByProductId(productId);
    }

    @Test
    void getImagesByProduct_AsNonAdmin_ThrowsAccessDenied() {
        mockUserAuthentication();

        assertThrows(CustomAccessDeniedException.class, () ->
                productImageService.getImagesByProduct(productId, authentication));

        verifyNoInteractions(productRepository, productImageRepository);
    }

    @Test
    void getImagesByProduct_ProductNotFound_ThrowsResourceNotFoundException() {
        mockAdminAuthentication();
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                productImageService.getImagesByProduct(productId, authentication));

        verify(productRepository, times(1)).existsById(productId);
        verify(productImageRepository, never()).findProductImagesByProductId(anyLong());
    }
}
