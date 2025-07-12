package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.ProductRating;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar una calificación de producto en la API REST de PodStream.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Data
public class ProductRatingDTO {

    private Long id;

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser positivo")
    private Long clientId;

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long productId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación no puede exceder 5")
    private Integer rating;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProductRatingDTO() {}

    public ProductRatingDTO(ProductRating rating) {
        this.id = rating.getId();
        this.clientId = rating.getClient().getId();
        this.productId = rating.getProduct().getId();
        this.rating = rating.getRating();
        this.active = rating.isActive();
        this.createdAt = rating.getCreatedAt();
        this.updatedAt = rating.getUpdatedAt();
    }
}