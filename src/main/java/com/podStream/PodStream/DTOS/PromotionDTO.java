package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Promotion;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar una promoción en la API REST de PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
@Data
public class PromotionDTO {

    private Long id;

    @NotBlank(message = "El código de promoción es obligatorio")
    @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 caracteres")
    private String code;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @Min(value = 0, message = "El descuento debe ser al menos 0%")
    @Max(value = 100, message = "El descuento no puede exceder 100%")
    private Double discountPercentage;

    @NotNull(message = "La fecha de validez es obligatoria")
    private LocalDateTime validUntil;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public PromotionDTO() {}

    public PromotionDTO(Promotion promotion) {
        this.id = promotion.getId();
        this.code = promotion.getCode();
        this.discountPercentage = promotion.getDiscountPercentage();
        this.validUntil = promotion.getValidUntil();
        this.active = promotion.getActive();
        this.createdAt = promotion.getCreatedAt();
        this.updatedAt = promotion.getUpdatedAt();
    }
}