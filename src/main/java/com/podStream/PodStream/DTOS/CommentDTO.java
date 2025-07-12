package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para representar un comentario en PodStream.
 */
@Data
public class CommentDTO {

    private Long id;

    @NotBlank(message = "El cuerpo del comentario no puede estar vacío")
    private String body;

    private LocalDateTime date;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    private boolean active;

    public CommentDTO() {}

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.body = comment.getBody();
        this.date = comment.getDate();
        this.clientId = comment.getClient().getId();
        this.productId = comment.getProduct().getId();
        this.active = comment.isActive();
    }
}