package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.PromotionDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Interfaz para gestionar promociones en PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
public interface PromotionService {

    /**
     * Crea una nueva promoción.
     *
     * @param promotionDTO La promoción a crear.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la promoción creada.
     */
    PromotionDTO createPromotion(PromotionDTO promotionDTO, Authentication authentication);

    /**
     * Obtiene una promoción por su ID.
     *
     * @param id El ID de la promoción.
     * @return El DTO de la promoción.
     */
    PromotionDTO getPromotion(Long id);

    /**
     * Obtiene una promoción por su código.
     *
     * @param code El código de la promoción.
     * @return El DTO de la promoción.
     */
    PromotionDTO getPromotionByCode(String code);

    /**
     * Obtiene todas las promociones activas.
     *
     * @return Lista de DTOs de promociones.
     */
    List<PromotionDTO> getAllPromotions();

    /**
     * Actualiza una promoción existente.
     *
     * @param id El ID de la promoción.
     * @param promotionDTO Los datos actualizados.
     * @param authentication La autenticación del usuario.
     * @return El DTO de la promoción actualizada.
     */
    PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO, Authentication authentication);

    /**
     * Elimina (soft delete) una promoción.
     *
     * @param id El ID de la promoción.
     * @param authentication La autenticación del usuario.
     */
    void deletePromotion(Long id, Authentication authentication);
}