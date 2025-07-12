package com.podStream.PodStream.DTOS;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO para recibir solicitudes de creación o actualización de detalles de órdenes en PodStream.
 */
@Data
public class DetailsRequestDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    private String productName;

    @Positive(message = "La cantidad debe ser positiva")
    private Integer quantity;

    @Positive(message = "El precio debe ser positivo")
    private Double price;

    private String description;

    @NotNull(message = "El ID de la orden de compra es obligatorio")
    @Min(value = 1, message = "El ID de la orden de compra debe ser mayor que 0")
    private Long purchaseOrderId;

    @NotNull(message = "El ID del producto es obligatorio")
    @Min(value = 1, message = "El ID del producto debe ser mayor que 0")
    private Long productId;
}