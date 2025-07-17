package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.model.ProductImage;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProductImageService {

    ProductImage addImageToProduct(Long productId, ProductImage image, Authentication authentication);

    void deleteImage(Long imageId, Authentication authentication);

    List<ProductImage> getImagesByProduct(Long productId, Authentication authentication);
}