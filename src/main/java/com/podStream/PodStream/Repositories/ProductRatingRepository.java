package com.podStream.PodStream.Repositories;

import com.podStream.PodStream.Models.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface ProductRatingRepository extends JpaRepository <ProductRating, Long> {

    List<ProductRating> findByProductId(Long productId);

    List<ProductRating> findByClientId(Long clientId);

    @Query("SELECT p FROM ProductRating p WHERE p.client.id = :clientId AND p.product.id = :productId")
    Optional<ProductRating> findByClientIdAndProductId(Long clientId, Long productId);
}
