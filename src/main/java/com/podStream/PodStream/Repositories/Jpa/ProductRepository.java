package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones CRUD de Product.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findByPriceBetweenAndActiveTrue(double minPrice, double maxPrice);

    List<Product> findByOrderBySalesCountDesc(Pageable pageable);
    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    boolean existsByCategoryId(Long id);
}

