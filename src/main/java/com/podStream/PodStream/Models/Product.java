package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa un producto en el catálogo del e-commerce PodStream.
 *
 * Esta entidad almacena información relevante de un producto, incluyendo
 * su nombre, descripción, precio, stock, y atributos para la categorización.
 * Gestiona relaciones con comentarios, detalles de órdenes, interacciones de
 * clientes, calificaciones, y elementos del carrito.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.2
 * @since 2025-07-09
 */
@Entity
@Table(name = "products")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String name;

    @Column(length = 10000)
    private String description;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private double price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    @Column(name = "sales_count")
    @Min(value = 0, message = "El contador de ventas no puede ser negativo")
    private int salesCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryProduct category;

    @Enumerated(EnumType.STRING)
    private ColorProduct color;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 1, message = "El descuento no puede exceder el 100%")
    private double discount;

    @Pattern(regexp = "^(http|https)://.*$", message = "La URL de la imagen debe ser válida")
    private String image;

    @PositiveOrZero(message = "La puntuación promedio no puede ser negativa")
    private double averageRating;

    @PositiveOrZero(message = "La suma de calificaciones no puede ser negativa")
    private double totalRatingPoints;

    @ElementCollection
    @CollectionTable(name = "product_image_collection", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Size(max = 10, message = "Máximo 10 imágenes adicionales")
    private List<String> imageCollection = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Details> details = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<ClientInteraction> clientInteractions = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProductRating> productRatings = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<CartItem> cartItems = new HashSet<>();

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime updatedAt;

}