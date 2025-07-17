package com.danialrekhman.productservicenocturne.service;

import com.danialrekhman.productservicenocturne.model.Product;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProductService {

        Product createProduct(Product product, Authentication authentication);

        Product updateProduct(Long id, Product updatedProduct, Authentication authentication);

        void deleteProduct(Long id, Authentication authentication);

        Product getProductById(Long id);

        List<Product> getAllProducts();

        List<Product> getProductsByCategory(Long categoryId);

        List<Product> searchProducts(String keyword); // поиск по имени/описанию

        boolean isAvailableById(Long id);
}