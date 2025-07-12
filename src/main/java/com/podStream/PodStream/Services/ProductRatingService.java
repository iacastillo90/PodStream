package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.ProductRatingDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar calificaciones de productos en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
public interface ProductRatingService {

    /**
     * Crea una nueva calificación para un producto.
     *
     * @param ratingDTO La calificación a crear.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la calificación creada.
     */
    ProductRatingDTO createRating(ProductRatingDTO ratingDTO, Authentication authentication);

    /**
     * Obtiene una calificación por su ID.
     *
     * @param id El ID de la calificación.
     * @return El DTO de la calificación.
     */
    ProductRatingDTO getRating(Long id);

    /**
     * Obtiene todas las calificaciones activas para un producto.
     *
     * @param productId El ID del producto.
     * @return Lista de DTOs de calificaciones.
     */
    List<ProductRatingDTO> getRatingsByProduct(Long productId);

    /**
     * Obtiene todas las calificaciones activas para un cliente.
     *
     * @param clientId El ID del cliente.
     * @return Lista de DTOs de calificaciones.
     */
    List<ProductRatingDTO> getRatingsByClient(Long clientId);

    /**
     * Actualiza una calificación existente.
     *
     * @param id El ID de la calificación.
     * @param ratingDTO Los datos actualizados.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la calificación actualizada.
     */
    ProductRatingDTO updateRating(Long id, ProductRatingDTO ratingDTO, Authentication authentication);

    /**
     * Elimina (soft delete) una calificación.
     *
     * @param id El ID de la calificación.
     * @param authentication La autenticación del usuario.
     */
    void deleteRating(Long id, Authentication authentication);
}