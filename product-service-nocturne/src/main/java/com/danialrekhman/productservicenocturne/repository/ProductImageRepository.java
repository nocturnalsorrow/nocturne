package com.danialrekhman.productservicenocturne.repository;

import com.danialrekhman.productservicenocturne.model.ProductImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface ProductImageRepository extends JpaRepository <ProductImage, Long> {

    List<ProductImage> findProductImagesByProductId(Long productId);
}