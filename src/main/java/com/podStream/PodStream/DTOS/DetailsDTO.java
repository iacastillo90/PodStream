package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Details;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar los detalles de una orden de compra en PodStream.
 */
@Data
public class DetailsDTO {

    private Long id;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    private String productName;

    @Positive(message = "La cantidad debe ser positiva")
    private Integer quantity;

    @Positive(message = "El precio debe ser positivo")
    private Double price;

    private String description;

    @NotNull(message = "El ID de la orden de compra es obligatorio")
    private Long purchaseOrderId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    private LocalDateTime createdDate;

    private boolean active;

    public DetailsDTO() {}

    public DetailsDTO(Details details) {
        this.id = details.getId();
        this.productName = details.getProductName();
        this.quantity = details.getQuantity();
        this.price = details.getPrice();
        this.description = details.getDescription();
        this.purchaseOrderId = details.getPurchaseOrder().getId();
        this.productId = details.getProduct().getId();
        this.createdDate = details.getCreatedDate();
        this.active = details.isActive();
    }
}