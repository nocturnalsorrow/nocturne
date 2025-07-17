package com.danialrekhman.productservicenocturne.repository;

import com.danialrekhman.productservicenocturne.model.Category;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    List<Category> findAllByParentId(Long parentId);

    List<Category> findAllByParentIsNull();
}