package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.ColorProduct;
import com.podStream.PodStream.Models.ProductDocument;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO para representar un documento de producto en la API REST de PodStream.
 */
@Data
public class ProductDocumentDTO {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String name;

    private String description;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private Long categoryId;

    private ColorProduct color;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 1, message = "El descuento no puede exceder el 100%")
    private Double discount;

    @PositiveOrZero(message = "La puntuación promedio no puede ser negativa")
    private Double averageRating;

    private boolean active;

    public ProductDocumentDTO() {}

    public ProductDocumentDTO(ProductDocument document) {
        this.id = document.getId();
        this.name = document.getName();
        this.description = document.getDescription();
        this.price = document.getPrice();
        this.stock = document.getStock();
        this.categoryId = document.getCategoryId();
        this.color = document.getColor() != null ? ColorProduct.valueOf(document.getColor()) : null;
        this.discount = document.getDiscount();
        this.averageRating = document.getAverageRating();
        this.active = document.isActive();
    }
}