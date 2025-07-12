package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa una promoción en PodStream.
 * <p>Almacena el código de la promoción, el porcentaje de descuento, la fecha de validez y el estado activo, junto con auditoría de creación y actualización.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.0.0
 * @since 2025-07-09
 */
@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promotion_code", columnList = "code"),
        @Index(name = "idx_promotion_active", columnList = "active")
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de promoción es obligatorio")
    @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 caracteres")
    @Column(unique = true)
    private String code;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @Min(value = 0, message = "El descuento debe ser al menos 0%")
    @Max(value = 100, message = "El descuento no puede exceder 100%")
    private Double discountPercentage;

    @NotNull(message = "La fecha de validez es obligatoria")
    private LocalDateTime validUntil;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}