
package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.InteractionType;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class ClientInteractionDTO {

    private Long id;

    private Client client;

    private Product product;

    private InteractionType interactionType;

    private LocalDateTime timestamp;

    private String sessionId;

    private Integer quantity;

    public ClientInteractionDTO() {
    }

    public ClientInteractionDTO(Long id, Client client, Product product, InteractionType interactionType, LocalDateTime timestamp, String sessionId, Integer quantity) {
        this.id = id;
        this.client = client;
        this.product = product;
        this.interactionType = interactionType;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Product getProduct() {
        return product;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
