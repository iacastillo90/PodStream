package com.podStream.PodStream.Repositories.Jpa;

import com.podStream.PodStream.Models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Promotion en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Encuentra una promoción activa por su código.
     *
     * @param code El código de la promoción.
     * @return Optional con la promoción, si existe.
     */
    Optional<Promotion> findByCodeAndActiveTrue(String code);

    /**
     * Encuentra todas las promociones activas.
     *
     * @return Lista de promociones activas.
     */
    List<Promotion> findByActiveTrue();

    /**
     * Encuentra promociones activas válidas hasta una fecha dada.
     *
     * @param date La fecha límite.
     * @return Lista de promociones válidas.
     */
    List<Promotion> findByActiveTrueAndValidUntilAfter(LocalDateTime date);
}