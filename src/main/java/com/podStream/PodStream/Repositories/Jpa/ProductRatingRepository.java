package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de ProductRating en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Repository
public interface ProductRatingRepository extends JpaRepository<ProductRating, Long> {

    /**
     * Encuentra todas las calificaciones activas para un producto dado.
     *
     * @param productId El ID del producto.
     * @return Lista de calificaciones activas.
     */
    List<ProductRating> findByProductIdAndActiveTrue(Long productId);

    /**
     * Encuentra todas las calificaciones activas para un cliente dado.
     *
     * @param clientId El ID del cliente.
     * @return Lista de calificaciones activas.
     */
    List<ProductRating> findByClientIdAndActiveTrue(Long clientId);

    /**
     * Encuentra una calificación activa por cliente y producto.
     *
     * @param clientId El ID del cliente.
     * @param productId El ID del producto.
     * @return Optional con la calificación, si existe.
     */
    @Query("SELECT p FROM ProductRating p WHERE p.client.id = :clientId AND p.product.id = :productId AND p.active = true")
    Optional<ProductRating> findByClientIdAndProductIdAndActiveTrue(Long clientId, Long productId);

    /**
     * Calcula la calificación promedio para un producto dado.
     *
     * @param productId El ID del producto.
     * @return La calificación promedio, o null si no hay calificaciones.
     */
    @Query("SELECT AVG(p.rating) FROM ProductRating p WHERE p.product.id = :productId AND p.active = true")
    Double findAverageRatingByProductId(Long productId);

    List<ProductRating> findByProductId(Long id);

    Optional<ProductRating> findByClientIdAndProductId(Long id, Long id1);
}