package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p ORDER BY p.salesCount DESC")
    List<Product> findTopPopularProducts(Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}

