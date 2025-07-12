package com.podStream.PodStream.DTOS;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para recibir solicitudes de creación o actualización de comentarios en PodStream.
 */
@Data
public class CommentRequestDTO {

    @NotBlank(message = "El cuerpo del comentario no puede estar vacío")
    private String body;

    @NotNull(message = "El ID del cliente es obligatorio")
    @Min(value = 1, message = "El ID del cliente debe ser mayor que 0")
    private Long clientId;

    @NotNull(message = "El ID del producto es obligatorio")
    @Min(value = 1, message = "El ID del producto debe ser mayor que 0")
    private Long productId;
}