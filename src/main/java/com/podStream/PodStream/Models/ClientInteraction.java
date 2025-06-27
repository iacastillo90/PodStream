package com.podStream.PodStream.Models;

import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * Entidad que representa una interacción de un cliente con un producto en PodStream.
 * <p>Almacena detalles como el tipo de interacción (vista, compra, etc.), el cliente y producto involucrados,
 * y metadatos como la sesión y la cantidad.
 *
 * @author [Tu Nombre o Equipo PodStream]
 * @since 0.0.1-SNAPSHOT
 */
@Entity
@Table(name = "client_interaction", indexes = {
        @Index(name = "idx_client_id", columnList = "client_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
public class ClientInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Product product;

    @NotNull(message = "El tipo de interacción es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @NotNull(message = "La marca temporal es obligatoria")
    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    private String sessionId;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer quantity;

    public ClientInteraction() {
    }

    public ClientInteraction(Client client, Product product, InteractionType interactionType, String sessionId, Integer quantity) {
        this.client = client;
        this.product = product;
        this.interactionType = interactionType;
        this.sessionId = sessionId;
        this.quantity = quantity;
    }

    // Getters y Setters
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

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}