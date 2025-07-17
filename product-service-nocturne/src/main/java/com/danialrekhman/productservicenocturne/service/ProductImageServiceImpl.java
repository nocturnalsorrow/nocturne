package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.exception.CustomAccessDeniedException;
import com.danialrekhman.productservicenocturne.exception.ResourceNotFoundException;
import com.danialrekhman.productservicenocturne.model.Product;
import com.danialrekhman.productservicenocturne.model.ProductImage;
import com.danialrekhman.productservicenocturne.repository.ProductImageRepository;
import com.danialrekhman.productservicenocturne.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    private final ProductRepository productRepository;

    @Override
    public ProductImage addImageToProduct(Long productId, ProductImage image, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can add image to product.");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " not found for image creation."));
        image.setProduct(product);
        return productImageRepository.save(image);
    }

    @Override
    public void deleteImage(Long imageId, Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can delete image.");
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image with id " + imageId + " not found for deletion."));
        productImageRepository.delete(image);
    }

    @Override
    public List<ProductImage> getImagesByProduct(Long productId, Authentication authentication) {
        if(!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can retrieve images by product.");
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product with id " + productId + " not found for image retrieval by product ID.");
        return productImageRepository.findProductImagesByProductId(productId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}