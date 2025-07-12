package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa una calificación de un producto por parte de un cliente en PodStream.
 * <p>Almacena la relación entre un cliente, un producto, la calificación asignada (1-5) y las fechas de creación y actualización.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1.0
 * @since 2025-07-09
 */
@Entity
@Table(name = "product_ratings", indexes = {
        @Index(name = "idx_client_id", columnList = "client_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class ProductRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Product product;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación no puede exceder 5")
    private Integer rating;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedAt;
}