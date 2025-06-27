package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * Entidad que representa una calificación de un producto por parte de un cliente en PodStream.
 * <p>Almacena la relación entre un cliente, un producto, la calificación asignada y la fecha de la calificación.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 0.0.1-SNAPSHOT
 */
@Entity
@Table(name = "product_rating", indexes = {
        @Index(name = "idx_client_id", columnList = "client_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
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
    @Min(value = 1, message = "La calificación debe ser al menos 1.0")
    @Max(value = 5, message = "La calificación no puede exceder 5.0")
    private Double rating;

    @NotNull(message = "La marca temporal es obligatoria")
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public ProductRating() {
    }

    public ProductRating(Client client, Product product, Double rating, LocalDateTime timestamp) {
        this.client = client;
        this.product = product;
        this.rating = rating;
        this.timestamp = timestamp;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}