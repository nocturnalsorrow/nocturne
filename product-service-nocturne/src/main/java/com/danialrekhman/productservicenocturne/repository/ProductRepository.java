package com.danialrekhman.productservicenocturne.repository;

import com.danialrekhman.productservicenocturne.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface ProductRepository  extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    boolean existsByIdAndAvailableTrue(Long id);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByAvailableTrue();

    List<Product> findByCategoryIdAndAvailableTrue(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);
}