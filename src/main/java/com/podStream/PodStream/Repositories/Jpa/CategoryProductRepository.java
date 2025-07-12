package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.CategoryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryProductRepository extends JpaRepository<CategoryProduct, Long> {
    Optional<CategoryProduct> findByNameAndActiveTrue(String name);
    List<CategoryProduct> findByActiveTrue();
}
