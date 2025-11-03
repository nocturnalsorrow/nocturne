package com.danialrekhman.productservicenocturne.repository;

import com.danialrekhman.productservicenocturne.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository  extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    boolean existsByIdAndAvailableTrue(Long id);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByAvailableTrue();

    List<Product> findByCategoryIdAndAvailableTrue(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByQuantityGreaterThan(int minQuantity);

    Optional<Product> findByIdAndQuantityGreaterThan(Long id, int minQuantity);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :amount WHERE p.id = :id AND p.quantity >= :amount")
    int decreaseStock(@Param("id") Long id, @Param("amount") int amount);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :amount WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("amount") int amount);
}