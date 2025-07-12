
package com.podStream.PodStream.DTOS;

import com.esotericsoftware.kryo.NotNull;
import com.podStream.PodStream.Models.ClientInteraction;
import com.podStream.PodStream.Models.InteractionType;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar una interacción de un cliente con un producto en PodStream.
 */
@Data
public class ClientInteractionDTO {

    private Long id;

    @NotNull
    @Min(value = 1, message = "El ID del cliente debe ser mayor que 0")
    private Long clientId;

    @NotNull
    @Min(value = 1, message = "El ID del producto debe ser mayor que 0")
    private Long productId;

    @NotNull
    private InteractionType interactionType;

    private LocalDateTime timestamp;

    private String sessionId;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer quantity;

    private boolean active;

    public ClientInteractionDTO() {}

    public ClientInteractionDTO(ClientInteraction interaction) {
        this.id = interaction.getId();
        this.clientId = interaction.getClient().getId();
        this.productId = interaction.getProduct().getId();
        this.interactionType = interaction.getInteractionType();
        this.timestamp = interaction.getTimestamp();
        this.sessionId = interaction.getSessionId();
        this.quantity = interaction.getQuantity();
        this.active = interaction.isActive();
    }
}
